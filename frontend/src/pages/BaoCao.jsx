import { useMemo, useState } from 'react';
import { api } from '../api/endpoints.js';
import { Donut, HBarChart } from '../components/Charts.jsx';
import DataTable, { IconButton, RowActions } from '../components/DataTable.jsx';
import { DatePicker, Field, MonthPicker, Select } from '../components/Fields.jsx';
import Icon from '../components/Icon.jsx';
import Modal from '../components/Modal.jsx';
import { useToast } from '../components/Toast.jsx';
import { Badge, Card, ErrorBox, PageHeader, StatCard, Tabs } from '../components/Ui.jsx';
import { useAsync } from '../hooks/useAsync.js';
import { useSinhVienOptions } from '../hooks/useOptions.js';
import { fmtDate, fmtDateTime, fmtMonth, firstDayOfMonth, localDate, localMonth, money, sum } from '../utils/format.js';

function useExport() {
  const toast = useToast();
  const [busy, setBusy] = useState(false);
  async function run(loai, params, fileName) {
    setBusy(true);
    try {
      await api.baoCao.csv(loai, params, fileName);
      toast.success(`Đã tải file ${fileName}`);
    } catch (e) {
      toast.error(e.message);
    } finally {
      setBusy(false);
    }
  }
  return { busy, run };
}

function ExportButton({ onClick, busy }) {
  return (
    <button type="button" className="btn btn-success" onClick={onClick} disabled={busy}>
      <Icon name="download" size={16} /> {busy ? 'Đang tải...' : 'Xuất file Excel'}
    </button>
  );
}

// Truy vấn 1
function InvoiceModal({ row, onClose }) {
  const [year, month] = row.thang.split('-').map(Number);
  const lastDay = String(new Date(year, month, 0).getDate()).padStart(2, '0');
  const usage = useAsync(
    () => api.suDung.list({ maSV: row.maSV, tuNgay: `${row.thang}-01`, denNgay: `${row.thang}-${lastDay}` }),
    [row.maSV, row.thang, lastDay],
  );
  const veXe = Number(row.tienVeThang) + Number(row.tienPhatSinhXe);

  return (
    <Modal
      title="Hóa đơn sinh viên"
      onClose={onClose}
      size="lg"
      footer={
        <>
          <button className="btn btn-ghost" onClick={onClose}>
            Đóng
          </button>
          <button className="btn btn-primary" onClick={() => window.print()} disabled={usage.loading || !!usage.error}>
            <Icon name="printer" size={16} /> In hóa đơn
          </button>
        </>
      }
    >
      <div className="invoice print-area">
        <h2>HÓA ĐƠN KÝ TÚC XÁ</h2>
        <p className="invoice-month">Tháng {fmtMonth(row.thang)}</p>
        <dl className="kv">
          <div><dt>Họ và tên</dt><dd>{row.hoTen}</dd></div>
          <div><dt>Mã SV</dt><dd>{row.maSV}</dd></div>
          <div><dt>Số CCCD</dt><dd>{row.cccd}</dd></div>
          <div><dt>Ngày sinh</dt><dd>{fmtDate(row.ngaySinh)}</dd></div>
          <div><dt>Lớp</dt><dd>{row.lop}</dd></div>
          <div><dt>Phòng</dt><dd>{row.phongTinhTien || 'Không phát sinh tiền phòng'}</dd></div>
        </dl>
        <table className="table invoice-table">
          <thead>
            <tr>
              <th>Khoản thu</th>
              <th className="ta-right">Số tiền</th>
            </tr>
          </thead>
          <tbody>
            <tr><td>Tiền phòng (tính chẵn tháng)</td><td className="ta-right">{money(row.tienPhong)}</td></tr>
            <tr><td>Tiền dịch vụ</td><td className="ta-right">{money(row.tienDichVu)}</td></tr>
            <tr><td>Vé gửi xe tháng</td><td className="ta-right">{money(row.tienVeThang)}</td></tr>
            <tr><td>Phát sinh gửi/lấy xe</td><td className="ta-right">{money(row.tienPhatSinhXe)}</td></tr>
          </tbody>
          <tfoot>
            <tr>
              <td><b>Tổng tiền phải trả</b></td>
              <td className="ta-right"><b>{money(row.tongTien)}</b></td>
            </tr>
          </tfoot>
        </table>

        <h4>Chi tiết dịch vụ đã sử dụng trong tháng</h4>
        <ErrorBox error={usage.error} onRetry={usage.reload} />
        {usage.loading && <p className="muted small">Đang tải...</p>}
        {usage.data && usage.data.length === 0 && <p className="muted small">Không sử dụng dịch vụ nào.</p>}
        {usage.data && usage.data.length > 0 && (
          <table className="table invoice-table">
            <thead>
              <tr>
                <th>Thời gian</th>
                <th>Dịch vụ</th>
                <th className="ta-right">Số lượng</th>
                <th className="ta-right">Thành tiền</th>
              </tr>
            </thead>
            <tbody>
              {usage.data.map((u) => (
                <tr key={`${u.maDV}|${u.thoiGianSD}`}>
                  <td>{fmtDateTime(u.thoiGianSD)}</td>
                  <td>{u.tenDV}</td>
                  <td className="ta-right">{u.soLuong}</td>
                  <td className="ta-right">{money(u.thanhTien)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        <p className="muted small">Phí gửi xe tháng này gồm vé tháng và tiền phát sinh: {money(veXe)}.</p>
      </div>
    </Modal>
  );
}

function HoaDonTab() {
  const sv = useSinhVienOptions();
  const exp = useExport();
  const [thang, setThang] = useState(localMonth());
  const [maSV, setMaSV] = useState('');
  const [applied, setApplied] = useState({ thang: localMonth(), maSV: '' });
  const list = useAsync(() => api.baoCao.hoaDon(applied), [applied]);
  const phongs = useAsync(() => api.phong.list({}), []);
  const tq = useAsync(() => api.tongQuan(), []);
  const [selected, setSelected] = useState(() => new Set());
  const [invoice, setInvoice] = useState(null);
  const rows = list.data || [];

  const key = (r) => `${r.maSV}|${r.thang}`;
  const total = sum(rows, (r) => r.tongTien);
  const chosen = rows.filter((r) => selected.has(key(r)));
  const fullRooms = (phongs.data || []).filter((p) => p.conCho === 0).length;
  const showMonth = !applied.thang;

  const donut = [
    { key: 'phong', label: 'Tiền phòng', value: sum(rows, (r) => r.tienPhong), color: '#1769e0' },
    { key: 'dv', label: 'Dịch vụ', value: sum(rows, (r) => r.tienDichVu), color: '#22a06b' },
    { key: 'xe', label: 'Gửi xe', value: sum(rows, (r) => Number(r.tienVeThang) + Number(r.tienPhatSinhXe)), color: '#f59f00' },
  ];

  function search(e) {
    e.preventDefault();
    setSelected(new Set());
    setApplied({ thang, maSV });
  }

  const columns = [
    { key: 'maSV', title: 'Mã SV', render: (r) => <b>{r.maSV}</b> },
    { key: 'hoTen', title: 'Họ và tên' },
    { key: 'soPhong', title: 'Số phòng', render: (r) => r.soPhong || r.phongTinhTien || '-' },
    ...(showMonth ? [{ key: 'thang', title: 'Tháng', render: (r) => fmtMonth(r.thang) }] : []),
    { key: 'tienPhong', title: 'Tiền phòng (tháng)', align: 'right', render: (r) => money(r.tienPhong) },
    { key: 'tienDichVu', title: 'Tiền dịch vụ', align: 'right', render: (r) => money(r.tienDichVu) },
    {
      key: 'phiXe',
      title: 'Phí gửi xe',
      align: 'right',
      render: (r) => (
        <span title={`Vé tháng ${money(r.tienVeThang)} + phát sinh ${money(r.tienPhatSinhXe)}`}>
          {money(Number(r.tienVeThang) + Number(r.tienPhatSinhXe))}
        </span>
      ),
    },
    { key: 'tongTien', title: 'Tổng tiền phải trả', align: 'right', render: (r) => <b>{money(r.tongTien)}</b> },
    {
      key: 'actions',
      title: '',
      align: 'right',
      render: (r) => (
        <RowActions>
          <IconButton icon="printer" title="Xem và in hóa đơn" onClick={() => setInvoice(r)} />
        </RowActions>
      ),
    },
  ];

  return (
    <>
      <div className="report-top">
        <Card className="filter-card">
          <form className="filter-form" onSubmit={search}>
            <Field label="Tháng">
              <MonthPicker value={thang} onChange={(e) => setThang(e.target.value)} placeholder="Tất cả các tháng" allowClear />
            </Field>
            <Field label="Sinh viên">
              <Select value={maSV} onChange={(e) => setMaSV(e.target.value)} options={sv.options} placeholder="Tất cả sinh viên (in danh sách)" />
            </Field>
            <div className="filter-actions">
              <button className="btn btn-primary" type="submit">
                <Icon name="search" size={16} /> Tìm kiếm
              </button>
              <ExportButton busy={exp.busy} onClick={() => exp.run('hoa-don', { thang: applied.thang, maSV: applied.maSV }, `hoa-don${applied.thang ? `-${applied.thang}` : ''}.xlsx`)} />
            </div>
          </form>
          <p className="muted small">Để trống tháng để xem mọi tháng. Chọn một sinh viên để in hóa đơn riêng từng người.</p>
        </Card>
        <StatCard label={applied.thang ? `Tổng doanh thu tháng ${fmtMonth(applied.thang)}` : 'Tổng doanh thu'} value={money(total)} />
        <StatCard label="Phòng đã đầy" value={phongs.data ? `${fullRooms}/${phongs.data.length}` : '-'} />
        <StatCard label="Sinh viên nội trú" value={tq.data ? tq.data.soNguoiDangO : '-'} />
      </div>

      <div className="report-grid">
        <Card className="report-table-card">
          <ErrorBox error={list.error} onRetry={list.reload} />
          <DataTable
            columns={columns}
            rows={rows}
            rowKey={key}
            loading={list.loading}
            empty="Không có hóa đơn nào theo bộ lọc này"
            selection={{ selected, onChange: setSelected }}
            foot={
              rows.length > 0 && (
                <tr>
                  <td colSpan={columns.length + 1}>
                    <div className="foot-sum">
                      <span>{chosen.length > 0 ? `Đã chọn ${chosen.length} sinh viên: ${money(sum(chosen, (r) => r.tongTien))}` : `${rows.length} hóa đơn`}</span>
                      <b>Tổng cộng: {money(total)}</b>
                    </div>
                  </td>
                </tr>
              )
            }
          />
        </Card>
        <Card title="Cơ cấu doanh thu" className="report-chart-card">
          <Donut items={donut} centerLabel="Tổng thu" />
        </Card>
      </div>
      {invoice && <InvoiceModal row={invoice} onClose={() => setInvoice(null)} />}
    </>
  );
}

// Truy vấn 2
function DichVuSinhVienTab() {
  const exp = useExport();
  const [tuNgay, setTuNgay] = useState(firstDayOfMonth());
  const [denNgay, setDenNgay] = useState(localDate());
  const [applied, setApplied] = useState({ tuNgay: firstDayOfMonth(), denNgay: localDate() });
  const list = useAsync(() => api.baoCao.dichVuSinhVien(applied), [applied]);
  const rows = list.data || [];

  return (
    <Card>
      <form
        className="filter-form filter-inline"
        onSubmit={(e) => {
          e.preventDefault();
          setApplied({ tuNgay, denNgay });
        }}
      >
        <Field label="Từ ngày">
          <DatePicker value={tuNgay} onChange={(e) => setTuNgay(e.target.value)} required />
        </Field>
        <Field label="Đến ngày">
          <DatePicker value={denNgay} onChange={(e) => setDenNgay(e.target.value)} required />
        </Field>
        <div className="filter-actions">
          <button className="btn btn-primary" type="submit">
            <Icon name="search" size={16} /> Tìm kiếm
          </button>
          <ExportButton busy={exp.busy} onClick={() => exp.run('dich-vu-sinh-vien', applied, `dich-vu-sinh-vien-${applied.tuNgay}-${applied.denNgay}.xlsx`)} />
        </div>
      </form>
      <p className="muted small">
        Dịch vụ “Trông xe” gồm tiền vé tháng và tiền gửi/lấy xe phát sinh của các xe thuộc sinh viên.
      </p>
      <ErrorBox error={list.error} onRetry={list.reload} />
      <DataTable
        rows={rows}
        rowKey={(r) => `${r.maSV}|${r.tenDV}`}
        loading={list.loading}
        empty="Không có sinh viên nào sử dụng dịch vụ trong khoảng thời gian này"
        columns={[
          { key: 'maSV', title: 'Mã SV', render: (r) => <b>{r.maSV}</b> },
          { key: 'hoTen', title: 'Họ và tên' },
          { key: 'cccd', title: 'Số CCCD' },
          { key: 'ngaySinh', title: 'Ngày sinh', render: (r) => fmtDate(r.ngaySinh) },
          { key: 'lop', title: 'Lớp' },
          { key: 'queQuan', title: 'Quê quán' },
          { key: 'tenDV', title: 'Tên dịch vụ' },
          { key: 'tongGiaDV', title: 'Tổng giá dịch vụ', align: 'right', render: (r) => <b>{money(r.tongGiaDV)}</b> },
        ]}
        foot={
          rows.length > 0 && (
            <tr>
              <td colSpan={7}>
                <b>Tổng cộng</b>
              </td>
              <td className="ta-right">
                <b>{money(sum(rows, (r) => r.tongGiaDV))}</b>
              </td>
            </tr>
          )
        }
      />
    </Card>
  );
}

// Truy vấn 3
function khoangHienThi(kieu, ngay) {
  const d = new Date(`${ngay}T00:00:00`);
  if (Number.isNaN(d.getTime())) return '';
  if (kieu === 'THANG') {
    const dau = new Date(d.getFullYear(), d.getMonth(), 1);
    const cuoi = new Date(d.getFullYear(), d.getMonth() + 1, 0);
    return `${fmtDate(localDate(dau))} đến ${fmtDate(localDate(cuoi))}`;
  }
  const lech = (d.getDay() + 6) % 7; // thứ Hai = 0
  const dau = new Date(d.getFullYear(), d.getMonth(), d.getDate() - lech);
  const cuoi = new Date(d.getFullYear(), d.getMonth(), d.getDate() - lech + 6);
  return `${fmtDate(localDate(dau))} đến ${fmtDate(localDate(cuoi))}`;
}

function KhachThamTab() {
  const exp = useExport();
  const [kieu, setKieu] = useState('TUAN');
  const [ngay, setNgay] = useState(localDate());
  const [tuNgay, setTuNgay] = useState(firstDayOfMonth());
  const [denNgay, setDenNgay] = useState(localDate());
  const initial = { kieu: 'TUAN', ngay: localDate() };
  const [applied, setApplied] = useState(initial);
  const list = useAsync(() => api.baoCao.khachTham(applied), [applied]);
  const rows = list.data || [];

  function search(e) {
    e.preventDefault();
    setApplied(kieu === 'KHOANG' ? { tuNgay, denNgay } : { kieu, ngay });
  }

  const rangeText = applied.kieu ? khoangHienThi(applied.kieu, applied.ngay) : `${fmtDate(applied.tuNgay)} đến ${fmtDate(applied.denNgay)}`;

  return (
    <Card>
      <form className="filter-form filter-inline" onSubmit={search}>
        <Field label="Xem theo">
          <select className="input" value={kieu} onChange={(e) => setKieu(e.target.value)}>
            <option value="TUAN">Tuần</option>
            <option value="THANG">Tháng</option>
            <option value="KHOANG">Khoảng ngày</option>
          </select>
        </Field>
        {kieu === 'KHOANG' ? (
          <>
            <Field label="Từ ngày">
              <DatePicker value={tuNgay} onChange={(e) => setTuNgay(e.target.value)} required />
            </Field>
            <Field label="Đến ngày">
              <DatePicker value={denNgay} onChange={(e) => setDenNgay(e.target.value)} required />
            </Field>
          </>
        ) : (
          <Field label={kieu === 'TUAN' ? 'Một ngày trong tuần' : 'Một ngày trong tháng'}>
            <DatePicker value={ngay} onChange={(e) => setNgay(e.target.value)} required />
          </Field>
        )}
        <div className="filter-actions">
          <button className="btn btn-primary" type="submit">
            <Icon name="search" size={16} /> Tìm kiếm
          </button>
          <ExportButton busy={exp.busy} onClick={() => exp.run('khach-tham', applied, 'khach-tham.xlsx')} />
        </div>
      </form>
      <p className="muted small">Khoảng thời gian đang xem: {rangeText}</p>
      <ErrorBox error={list.error} onRetry={list.reload} />
      <DataTable
        rows={rows}
        rowKey={(r) => `${r.maSV}|${r.cccdKhach}`}
        loading={list.loading}
        empty="Không có khách nào đến thăm trong khoảng thời gian này"
        columns={[
          { key: 'maSV', title: 'Mã SV', render: (r) => <b>{r.maSV}</b> },
          { key: 'hoTenSV', title: 'Sinh viên' },
          { key: 'cccdSV', title: 'CCCD sinh viên' },
          { key: 'lop', title: 'Lớp' },
          { key: 'hoTenKhach', title: 'Khách đến thăm', render: (r) => <b>{r.hoTenKhach}</b> },
          { key: 'cccdKhach', title: 'CCCD khách' },
          { key: 'ngaySinhKhach', title: 'Ngày sinh khách', render: (r) => fmtDate(r.ngaySinhKhach) },
          { key: 'soLanDenChoi', title: 'Số lần đến chơi', align: 'right', render: (r) => <Badge tone="blue">{r.soLanDenChoi} lần</Badge> },
          {
            key: 'cacLanDen',
            title: 'Các lần đến',
            render: (r) => (
              <div className="chips">
                {r.cacLanDen.map((t) => (
                  <span key={t} className="chip">
                    {fmtDateTime(t)}
                  </span>
                ))}
              </div>
            ),
          },
        ]}
      />
    </Card>
  );
}

// Truy vấn 4
function DoanhThuTab() {
  const exp = useExport();
  const [thang, setThang] = useState(localMonth());
  const [moiThang, setMoiThang] = useState(false);
  const [applied, setApplied] = useState({ thang: localMonth() });
  const list = useAsync(() => api.baoCao.doanhThuDichVu(applied), [applied]);
  const rows = list.data || [];
  const showMonth = !applied.thang;

  const perService = useMemo(() => {
    const map = new Map();
    rows.forEach((r) => map.set(r.tenDV, (map.get(r.tenDV) || 0) + Number(r.tongDoanhThu)));
    return [...map.entries()].map(([label, value]) => ({ key: label, label, value }));
  }, [rows]);

  return (
    <Card>
      <form
        className="filter-form filter-inline"
        onSubmit={(e) => {
          e.preventDefault();
          setApplied(moiThang ? {} : { thang });
        }}
      >
        <Field label="Tháng">
          <MonthPicker value={thang} onChange={(e) => setThang(e.target.value)} disabled={moiThang} placeholder="Chọn tháng" allowClear={false} />
        </Field>
        <label className="check check-field">
          <input type="checkbox" checked={moiThang} onChange={(e) => setMoiThang(e.target.checked)} /> Xem mọi tháng
        </label>
        <div className="filter-actions">
          <button className="btn btn-primary" type="submit">
            <Icon name="search" size={16} /> Tìm kiếm
          </button>
          <ExportButton busy={exp.busy} onClick={() => exp.run('doanh-thu-dich-vu', applied, `doanh-thu-dich-vu${applied.thang ? `-${applied.thang}` : ''}.xlsx`)} />
        </div>
      </form>
      <ErrorBox error={list.error} onRetry={list.reload} />
      <div className="report-grid">
        <div className="report-table-card">
          <DataTable
            rows={rows}
            rowKey={(r) => `${r.maDV}|${r.thang}`}
            loading={list.loading}
            empty="Không có dữ liệu doanh thu"
            columns={[
              { key: 'maDV', title: 'Mã DV', render: (r) => <b>{r.maDV}</b> },
              { key: 'tenDV', title: 'Tên dịch vụ' },
              { key: 'donGia', title: 'Đơn giá', align: 'right', render: (r) => r.tenDV === 'Trông xe' ? '100.000 đ/vé tháng, 3.000 đ/lượt phát sinh' : money(r.donGia) },
              ...(showMonth ? [{ key: 'thang', title: 'Tháng', render: (r) => fmtMonth(r.thang) }] : []),
              { key: 'soLuong', title: 'SL dịch vụ', align: 'right', render: (r) => r.soLuong ?? '—' },
              { key: 'doanhThuDichVu', title: 'DT dịch vụ', align: 'right', render: (r) => money(r.doanhThuDichVu) },
              { key: 'doanhThuVeThang', title: 'DT vé tháng', align: 'right', render: (r) => money(r.doanhThuVeThang) },
              { key: 'doanhThuPhatSinh', title: 'DT phát sinh', align: 'right', render: (r) => money(r.doanhThuPhatSinh) },
              { key: 'tongDoanhThu', title: 'Tổng doanh thu', align: 'right', render: (r) => <b>{money(r.tongDoanhThu)}</b> },
            ]}
            foot={
              rows.length > 0 && (
                <tr>
                  <td colSpan={showMonth ? 8 : 7}>
                    <b>Tổng cộng</b>
                  </td>
                  <td className="ta-right">
                    <b>{money(sum(rows, (r) => r.tongDoanhThu))}</b>
                  </td>
                </tr>
              )
            }
          />
        </div>
        <Card title="Doanh thu từng dịch vụ" className="report-chart-card">
          {perService.length === 0 ? <div className="table-state">Chưa có dữ liệu</div> : <HBarChart items={perService} />}
        </Card>
      </div>
    </Card>
  );
}

export default function BaoCao() {
  const [tab, setTab] = useState('hoa-don');
  return (
    <>
      <PageHeader title="Hóa đơn và Báo cáo" subtitle="Tiền phải trả của sinh viên, dịch vụ đã dùng, khách đến thăm và doanh thu dịch vụ" />
      <p className="muted small report-note">Số liệu là số tiền phải trả theo đơn giá đang lưu</p>
      <Tabs
        tabs={[
          { key: 'hoa-don', label: 'Hóa đơn theo tháng' },
          { key: 'dich-vu', label: 'Dịch vụ của sinh viên' },
          { key: 'khach', label: 'Khách đến thăm' },
          { key: 'doanh-thu', label: 'Doanh thu dịch vụ' },
        ]}
        value={tab}
        onChange={setTab}
      />
      {tab === 'hoa-don' && <HoaDonTab />}
      {tab === 'dich-vu' && <DichVuSinhVienTab />}
      {tab === 'khach' && <KhachThamTab />}
      {tab === 'doanh-thu' && <DoanhThuTab />}
    </>
  );
}

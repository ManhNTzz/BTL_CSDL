import { api } from '../api/endpoints.js';
import Icon from '../components/Icon.jsx';
import { StackedBarChart } from '../components/Charts.jsx';
import { Badge, Card, ErrorBox, PageHeader, StatCard } from '../components/Ui.jsx';
import { useAsync } from '../hooks/useAsync.js';
import { REVENUE_SERIES } from '../utils/constants.js';
import { fmtDate, fmtDateTime, fmtMonth, money } from '../utils/format.js';

function AlertGroup({ title, tone = 'amber', items, render, to, hint }) {
  const shown = items.slice(0, 4);
  return (
    <div className="alert-group">
      <div className="alert-group-head">
        <span>{title}</span>
        <Badge tone={items.length ? tone : 'green'}>{items.length}</Badge>
      </div>
      {items.length === 0 ? (
        <p className="muted small">Không có</p>
      ) : (
        <ul>
          {shown.map(render)}
          {items.length > shown.length && <li className="muted">và {items.length - shown.length} mục khác...</li>}
        </ul>
      )}
      {items.length > 0 && (
        <a className="link small" href={`#/${to}`}>
          {hint}
        </a>
      )}
    </div>
  );
}

export default function Dashboard() {
  const tq = useAsync(() => api.tongQuan(), []);
  const dt = useAsync(() => api.doanhThuTheoThang(), []);
  const cb = useAsync(() => api.canhBao(), []);
  const t = tq.data;
  const c = cb.data;
  const fill = t && t.tongSoCho > 0 ? Math.round((t.soNguoiDangO / t.tongSoCho) * 100) : 0;

  function reloadAll() {
    tq.reload();
    dt.reload();
    cb.reload();
  }

  const chart = (dt.data || []).map((m) => ({
    label: fmtMonth(m.thang),
    values: { tienPhong: m.tienPhong, tienDichVu: m.tienDichVu, tienVeThang: m.tienVeThang, tienPhatSinhXe: m.tienPhatSinhXe },
  }));

  return (
    <>
      <PageHeader
        title="Tổng quan"
        subtitle="Tình hình ký túc xá hôm nay"
        actions={
          <button className="btn btn-ghost" onClick={reloadAll}>
            <Icon name="refresh" size={16} /> Làm mới
          </button>
        }
      />
      <ErrorBox error={tq.error || dt.error || cb.error} onRetry={reloadAll} />

      <div className="stats">
        <StatCard label="Sinh viên nội trú" value={t ? t.soNguoiDangO : '-'} sub={t ? `${t.soSinhVien} sinh viên trong hệ thống` : ''} />
        <StatCard label="Phòng" value={t ? t.soPhong : '-'} sub={t ? `${t.soChoTrong} chỗ trống / ${t.tongSoCho} chỗ` : ''} />
        <StatCard label="Tỷ lệ lấp đầy" value={`${fill}%`} sub="Số người đang ở / tổng số chỗ" />
        <StatCard label="Doanh thu tháng này" value={t ? money(t.doanhThuThangNay) : '-'} sub="Gồm tiền phòng, dịch vụ, gửi xe" />
        <StatCard label="Xe gửi vé tháng" value={t ? t.soXeVeThang : '-'} sub={t ? `${t.soVeThangThangNay} xe đã có vé tháng này` : ''} />
        <StatCard label="Khách đến thăm tháng này" value={t ? t.soLuotKhachThangNay : '-'} sub="Số lượt khách ra vào" />
      </div>

      <div className="grid-2">
        <Card title="Doanh thu theo tháng">
          {chart.length === 0 ? (
            <div className="table-state">{dt.loading ? 'Đang tải...' : 'Chưa có dữ liệu doanh thu'}</div>
          ) : (
            <StackedBarChart data={chart} series={REVENUE_SERIES} />
          )}
        </Card>

        <Card title="Cảnh báo và nhắc việc" actions={c && <span className="muted small">Ngày {fmtDate(c.ngay)}</span>}>
          {!c ? (
            <div className="table-state">{cb.loading ? 'Đang tải...' : 'Không có dữ liệu'}</div>
          ) : (
            <div className="alert-list">
              <AlertGroup
                title="Xe chưa có vé tháng này"
                items={c.xeChuaCoVeThangNay}
                to="gui-xe"
                hint="Đi tới Gửi xe tháng để gia hạn"
                render={(x) => (
                  <li key={x.bienSo}>
                    <b>{x.bienSo}</b> - {x.hoTenChuXe}
                  </li>
                )}
              />
              <AlertGroup
                title="Phòng đã đầy"
                tone="red"
                items={c.phongDayNguoi}
                to="phong"
                hint="Xem danh sách phòng"
                render={(p) => (
                  <li key={p.soPhong}>
                    <b>{p.soPhong}</b> - {p.soNguoiDangO}/{p.soNguoiToiDa} người
                  </li>
                )}
              />
              <AlertGroup
                title="Sinh viên chưa có phòng"
                items={c.sinhVienChuaCoPhong}
                to="phong"
                hint="Đăng ký ở phòng"
                render={(s) => (
                  <li key={s.maSV}>
                    <b>{s.maSV}</b> - {s.hoTen}
                  </li>
                )}
              />
              <AlertGroup
                title="Sắp hết hạn ở trong 30 ngày"
                items={c.sapHetHanO}
                to="phong"
                hint="Xem chi tiết ở phòng"
                render={(o) => (
                  <li key={`${o.maSV}|${o.soPhong}|${o.ngayBatDau}`}>
                    <b>{o.hoTen}</b> - phòng {o.soPhong}, đến {fmtDate(o.ngayKetThuc)}
                  </li>
                )}
              />
              <AlertGroup
                title="Xe gửi từ hôm trước chưa lấy"
                tone="red"
                items={c.xeGuiTuNgayTruoc}
                to="gui-xe"
                hint="Đi tới Gửi xe tháng"
                render={(l) => (
                  <li key={`${l.bienSo}|${l.thoiGianGui}`}>
                    <b>{l.bienSo}</b> - gửi lúc {fmtDateTime(l.thoiGianGui)}
                  </li>
                )}
              />
            </div>
          )}
        </Card>
      </div>
    </>
  );
}

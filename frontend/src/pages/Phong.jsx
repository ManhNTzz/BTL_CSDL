import { useState } from 'react';
import { api } from '../api/endpoints.js';
import DataTable, { IconButton, RowActions } from '../components/DataTable.jsx';
import { DatePicker, Field, Input, Select } from '../components/Fields.jsx';
import Icon from '../components/Icon.jsx';
import Modal, { ConfirmModal, FormModal } from '../components/Modal.jsx';
import { useToast } from '../components/Toast.jsx';
import { Badge, Card, ErrorBox, PageHeader, Progress, Tabs, Toolbar } from '../components/Ui.jsx';
import { useAsync } from '../hooks/useAsync.js';
import { useForm } from '../hooks/useForm.js';
import { usePhongOptions, useSinhVienOptions } from '../hooks/useOptions.js';
import { fmtDate, localDate, money, stayStatus } from '../utils/format.js';

function PhongForm({ initial, onClose, onSaved }) {
  const editing = Boolean(initial);
  const { values, bind } = useForm({
    soPhong: initial?.soPhong ?? '',
    loaiPhong: initial?.loaiPhong ?? '',
    donGia: initial?.donGia ?? '',
    soNguoiToiDa: initial?.soNguoiToiDa ?? '',
  });

  async function submit() {
    const body = {
      soPhong: values.soPhong.trim(),
      loaiPhong: values.loaiPhong.trim(),
      donGia: Number(values.donGia),
      soNguoiToiDa: Number(values.soNguoiToiDa),
    };
    if (editing) await api.phong.update(initial.soPhong, body);
    else await api.phong.create(body);
    onSaved(editing ? 'Đã cập nhật phòng' : 'Đã thêm phòng');
  }

  return (
    <FormModal title={editing ? `Sửa phòng ${initial.soPhong}` : 'Thêm phòng'} onClose={onClose} onSubmit={submit}>
      <Field label="Số phòng" required>
        <Input {...bind('soPhong')} required maxLength={10} disabled={editing} placeholder="VD: A101" />
      </Field>
      <Field label="Loại phòng" required>
        <Input {...bind('loaiPhong')} required maxLength={30} placeholder="VD: Thường, Cao cấp" />
      </Field>
      <Field label="Đơn giá mỗi tháng (đ)" required hint="Tiền thuê một người mỗi tháng">
        <Input type="number" min="0" {...bind('donGia')} required />
      </Field>
      <Field label="Số người tối đa" required>
        <Input type="number" min="1" {...bind('soNguoiToiDa')} required />
      </Field>
    </FormModal>
  );
}

function ResidentsModal({ phong, ngay, onClose }) {
  const res = useAsync(() => api.phong.sinhVien(phong.soPhong, ngay), [phong.soPhong, ngay]);
  return (
    <Modal title={`Sinh viên trong phòng ${phong.soPhong} (ngày ${fmtDate(ngay)})`} onClose={onClose} size="lg">
      <ErrorBox error={res.error} onRetry={res.reload} />
      <DataTable
        rows={res.data || []}
        rowKey={(r) => `${r.maSV}|${r.ngayBatDau}`}
        loading={res.loading}
        empty="Phòng đang trống"
        columns={[
          { key: 'maSV', title: 'Mã SV', render: (r) => <b>{r.maSV}</b> },
          { key: 'hoTen', title: 'Họ và tên' },
          { key: 'ngayBatDau', title: 'Ở từ ngày', render: (r) => fmtDate(r.ngayBatDau) },
          { key: 'ngayKetThuc', title: 'Đến ngày', render: (r) => (r.ngayKetThuc ? fmtDate(r.ngayKetThuc) : 'Chưa xác định') },
    { key: 'trangThai', title: 'Trạng thái', render: (r) => { const t = stayStatus(r); return <Badge tone={t.tone}>{t.label}</Badge>; } },
        ]}
      />
    </Modal>
  );
}

function PhongTab() {
  const toast = useToast();
  const [ngay, setNgay] = useState(localDate());
  const [conCho, setConCho] = useState(false);
  const list = useAsync(() => api.phong.list({ ngay, conCho: conCho ? 'true' : undefined }), [ngay, conCho]);
  const [form, setForm] = useState(null);
  const [viewing, setViewing] = useState(null);
  const [removing, setRemoving] = useState(null);
  const rows = list.data || [];

  const columns = [
    { key: 'soPhong', title: 'Số phòng', render: (r) => <b>{r.soPhong}</b> },
    { key: 'loaiPhong', title: 'Loại phòng' },
    { key: 'donGia', title: 'Đơn giá/tháng', align: 'right', render: (r) => money(r.donGia) },
    { key: 'soNguoiToiDa', title: 'Sức chứa', align: 'right' },
    {
      key: 'dangO',
      title: 'Đang ở',
      render: (r) => (
        <div className="occupancy">
          <Progress value={r.soNguoiDangO} max={r.soNguoiToiDa} />
          <span>
            {r.soNguoiDangO}/{r.soNguoiToiDa}
          </span>
        </div>
      ),
    },
    {
      key: 'conCho',
      title: 'Còn chỗ',
      render: (r) => (r.conCho > 0 ? <Badge tone="green">Còn {r.conCho} chỗ</Badge> : <Badge tone="red">Đã đầy</Badge>),
    },
    {
      key: 'actions',
      title: '',
      align: 'right',
      render: (r) => (
        <RowActions>
          <IconButton icon="users" title="Xem sinh viên trong phòng" onClick={() => setViewing(r)} />
          <IconButton icon="edit" title="Sửa" onClick={() => setForm(r)} />
          <IconButton icon="trash" title="Xóa" tone="danger" onClick={() => setRemoving(r)} />
        </RowActions>
      ),
    },
  ];

  return (
    <Card>
      <Toolbar>
        <div className="filters">
          <div className="inline-field">
            <span>Xem tình trạng ngày</span>
            <DatePicker value={ngay} onChange={(e) => setNgay(e.target.value || localDate())} />
          </div>
          <label className="check">
            <input type="checkbox" checked={conCho} onChange={(e) => setConCho(e.target.checked)} /> Chỉ phòng còn chỗ trống
          </label>
        </div>
        <button className="btn btn-primary" onClick={() => setForm('new')}>
          <Icon name="plus" size={16} /> Thêm phòng
        </button>
      </Toolbar>
      <ErrorBox error={list.error} onRetry={list.reload} />
      <DataTable columns={columns} rows={rows} rowKey={(r) => r.soPhong} loading={list.loading} empty="Không có phòng nào" />

      {form && (
        <PhongForm
          initial={form === 'new' ? null : form}
          onClose={() => setForm(null)}
          onSaved={(m) => {
            setForm(null);
            toast.success(m);
            list.reload();
          }}
        />
      )}
      {viewing && <ResidentsModal phong={viewing} ngay={ngay} onClose={() => setViewing(null)} />}
      {removing && (
        <ConfirmModal
          title="Xóa phòng"
          message={`Bạn có chắc muốn xóa phòng ${removing.soPhong}?`}
          onClose={() => setRemoving(null)}
          onConfirm={async () => {
            await api.phong.remove(removing.soPhong);
            setRemoving(null);
            toast.success('Đã xóa phòng');
            list.reload();
          }}
        />
      )}
    </Card>
  );
}

function DangKyForm({ maSV, svOptions, phongOptions, onClose, onSaved }) {
  const { values, bind } = useForm({ maSV, soPhong: '', ngayBatDau: localDate(), ngayKetThuc: '' });
  async function submit() {
    await api.oPhong.dangKy({
      maSV: values.maSV,
      soPhong: values.soPhong,
      ngayBatDau: values.ngayBatDau,
      ngayKetThuc: values.ngayKetThuc || null,
    });
    onSaved('Đã đăng ký ở phòng', values.maSV);
  }
  return (
    <FormModal title="Đăng ký ở phòng" onClose={onClose} onSubmit={submit} submitText="Đăng ký">
      <Field label="Sinh viên" required wide>
        <Select {...bind('maSV')} options={svOptions} placeholder="Chọn sinh viên" required />
      </Field>
      <Field label="Phòng" required wide hint="Hệ thống kiểm tra số người tối đa của phòng">
        <Select {...bind('soPhong')} options={phongOptions} placeholder="Chọn phòng" required />
      </Field>
      <Field label="Ở từ ngày" required>
        <DatePicker {...bind('ngayBatDau')} required />
      </Field>
      <Field label="Đến ngày" hint="Bỏ trống nếu chưa biết ngày ra">
        <DatePicker {...bind('ngayKetThuc')} />
      </Field>
    </FormModal>
  );
}

function ChuyenForm({ maSV, svOptions, phongOptions, onClose, onSaved }) {
  const { values, bind } = useForm({ maSV, soPhongMoi: '', ngayChuyen: localDate() });
  async function submit() {
    await api.oPhong.chuyen({ maSV: values.maSV, soPhongMoi: values.soPhongMoi, ngayChuyen: values.ngayChuyen });
    onSaved('Đã chuyển phòng', values.maSV);
  }
  return (
    <FormModal title="Chuyển phòng" onClose={onClose} onSubmit={submit} submitText="Chuyển phòng">
      <Field label="Sinh viên" required wide>
        <Select {...bind('maSV')} options={svOptions} placeholder="Chọn sinh viên" required />
      </Field>
      <Field label="Chuyển sang phòng" required wide>
        <Select {...bind('soPhongMoi')} options={phongOptions} placeholder="Chọn phòng mới" required />
      </Field>
      <Field label="Ngày chuyển" required hint="Phòng cũ kết thúc và phòng mới bắt đầu cùng ngày chuyển. Tiền tháng lấy đơn giá của đợt ở mới nhất trong tháng.">
        <DatePicker {...bind('ngayChuyen')} required />
      </Field>
    </FormModal>
  );
}

function KetThucForm({ row, onClose, onSaved }) {
  const { values, bind } = useForm({ ngayKetThuc: localDate() });
  async function submit() {
    await api.oPhong.ketThuc({
      maSV: row.maSV,
      soPhong: row.soPhong,
      ngayBatDau: row.ngayBatDau,
      ngayKetThuc: values.ngayKetThuc,
    });
    onSaved('Đã kết thúc ở phòng', row.maSV);
  }
  return (
    <FormModal title={`Kết thúc ở phòng ${row.soPhong}`} onClose={onClose} onSubmit={submit} submitText="Kết thúc">
      <Field label="Ngày kết thúc" required wide>
        <DatePicker {...bind('ngayKetThuc')} required min={row.ngayBatDau} />
      </Field>
    </FormModal>
  );
}

function OPhongTab() {
  const toast = useToast();
  const sv = useSinhVienOptions();
  const phongs = usePhongOptions();
  const [maSV, setMaSV] = useState('');
  const history = useAsync(() => (maSV ? api.oPhong.lichSu(maSV) : Promise.resolve([])), [maSV]);
  const [modal, setModal] = useState(null); // 'dangky' | 'chuyen' | {ketThuc: dòng}
  const rows = history.data || [];

  function saved(message, ma) {
    setModal(null);
    toast.success(message);
    phongs.reload();
    if (ma && ma !== maSV) setMaSV(ma);
    else history.reload();
  }

  const columns = [
    { key: 'soPhong', title: 'Phòng', render: (r) => <b>{r.soPhong}</b> },
    { key: 'loaiPhong', title: 'Loại phòng' },
    { key: 'donGia', title: 'Đơn giá/tháng', align: 'right', render: (r) => money(r.donGia) },
    { key: 'ngayBatDau', title: 'Ở từ ngày', render: (r) => fmtDate(r.ngayBatDau) },
    {
      key: 'ngayKetThuc',
      title: 'Đến ngày',
      render: (r) => (r.ngayKetThuc ? fmtDate(r.ngayKetThuc) : <span className="muted">Chưa xác định</span>),
    },
    { key: 'trangThai', title: 'Trạng thái', render: (r) => { const t=stayStatus(r); return <Badge tone={t.tone}>{t.label}</Badge>; } },
    {
      key: 'actions',
      title: '',
      align: 'right',
      render: (r) =>
        r.ngayKetThuc ? null : (
          <button className="btn btn-ghost btn-sm" onClick={() => setModal({ ketThuc: r })}>
            Kết thúc ở
          </button>
        ),
    },
  ];

  return (
    <Card>
      <Toolbar>
        <div className="filters">
          <label className="inline-field">
            <span>Sinh viên</span>
            <select className="input" value={maSV} onChange={(e) => setMaSV(e.target.value)}>
              <option value="">Chọn sinh viên để xem lịch sử ở phòng</option>
              {sv.options.map((o) => (
                <option key={o.value} value={o.value}>
                  {o.label}
                </option>
              ))}
            </select>
          </label>
        </div>
        <div className="btn-group">
          <button className="btn btn-primary" onClick={() => setModal('dangky')}>
            <Icon name="plus" size={16} /> Đăng ký ở phòng
          </button>
          <button className="btn btn-ghost" onClick={() => setModal('chuyen')} disabled={!maSV}>
            <Icon name="repeat" size={16} /> Chuyển phòng
          </button>
        </div>
      </Toolbar>
      <ErrorBox error={history.error || sv.error} onRetry={history.reload} />
      {!maSV ? (
        <div className="table-state">Hãy chọn một sinh viên để xem lịch sử ở phòng, hoặc bấm “Đăng ký ở phòng”.</div>
      ) : (
        <DataTable columns={columns} rows={rows} rowKey={(r) => `${r.soPhong}|${r.ngayBatDau}`} loading={history.loading} empty="Sinh viên chưa từng ở phòng nào" />
      )}

      {modal === 'dangky' && (
        <DangKyForm maSV={maSV} svOptions={sv.options} phongOptions={phongs.options} onClose={() => setModal(null)} onSaved={saved} />
      )}
      {modal === 'chuyen' && (
        <ChuyenForm maSV={maSV} svOptions={sv.options} phongOptions={phongs.options} onClose={() => setModal(null)} onSaved={saved} />
      )}
      {modal && modal.ketThuc && <KetThucForm row={modal.ketThuc} onClose={() => setModal(null)} onSaved={saved} />}
    </Card>
  );
}

export default function Phong() {
  const [tab, setTab] = useState('phong');
  return (
    <>
      <PageHeader title="Quản lý Phòng ở" subtitle="Danh sách phòng, đăng ký ở phòng, kết thúc và chuyển phòng" />
      <Tabs
        tabs={[
          { key: 'phong', label: 'Danh sách phòng' },
          { key: 'o', label: 'Ở phòng và chuyển phòng' },
        ]}
        value={tab}
        onChange={setTab}
      />
      {tab === 'phong' ? <PhongTab /> : <OPhongTab />}
    </>
  );
}

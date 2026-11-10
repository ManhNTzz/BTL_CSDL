import { useState } from 'react';
import { api } from '../api/endpoints.js';
import DataTable, { IconButton, RowActions } from '../components/DataTable.jsx';
import { DatePicker, DateTimePicker, Field, Input, MonthPicker, Select } from '../components/Fields.jsx';
import Icon from '../components/Icon.jsx';
import { ConfirmModal, FormModal } from '../components/Modal.jsx';
import { useToast } from '../components/Toast.jsx';
import { Badge, Card, ErrorBox, PageHeader, Tabs, Toolbar } from '../components/Ui.jsx';
import { useAsync } from '../hooks/useAsync.js';
import { useForm } from '../hooks/useForm.js';
import { useSinhVienOptions, useXeOptions } from '../hooks/useOptions.js';
import { SO_XE_TOI_DA } from '../utils/constants.js';
import { fmtDateTime, fmtMonth, localDate, localDateTimeInput, localMonth, money, withSeconds } from '../utils/format.js';

function XeForm({ svOptions, onClose, onSaved }) {
  const { values, bind } = useForm({ maSV: '', bienSo: '', loaiXe: 'Xe máy', thang: localMonth() });
  async function submit() {
    await api.xe.create({ bienSo: values.bienSo.trim(), loaiXe: values.loaiXe.trim(), maSV: values.maSV, thang: values.thang });
    onSaved('Đã đăng ký xe và vé tháng đầu tiên');
  }
  return (
    <FormModal title="Đăng ký xe gửi vé tháng" onClose={onClose} onSubmit={submit} submitText="Đăng ký">
      <Field label="Sinh viên" required wide hint={`Mỗi sinh viên đăng ký tối đa ${SO_XE_TOI_DA} xe; phải có đợt ở giao với tháng vé`}>
        <Select {...bind('maSV')} options={svOptions} placeholder="Chọn sinh viên" required />
      </Field>
      <Field label="Tháng vé đầu tiên" required wide hint="Tạo xe và vé 100.000 đồng trong cùng một lần lưu.">
        <MonthPicker {...bind('thang')} allowClear={false} />
      </Field>
      <Field label="Biển số xe" required>
        <Input {...bind('bienSo')} required maxLength={15} placeholder="VD: 29B1-111.11" />
      </Field>
      <Field label="Loại xe" required>
        <Input {...bind('loaiXe')} required maxLength={30} list="loai-xe" />
        <datalist id="loai-xe">
          <option value="Xe máy" />
          <option value="Xe đạp điện" />
          <option value="Xe đạp" />
        </datalist>
      </Field>
    </FormModal>
  );
}

function XeTab() {
  const toast = useToast();
  const sv = useSinhVienOptions();
  const [maSV, setMaSV] = useState('');
  const list = useAsync(() => api.xe.list(maSV), [maSV]);
  const [creating, setCreating] = useState(false);
  const [removing, setRemoving] = useState(null);
  const rows = list.data || [];

  const columns = [
    { key: 'bienSo', title: 'Biển số', render: (r) => <b>{r.bienSo}</b> },
    { key: 'loaiXe', title: 'Loại xe' },
    { key: 'maSV', title: 'Mã SV' },
    { key: 'hoTenChuXe', title: 'Chủ xe' },
    {
      key: 'actions',
      title: '',
      align: 'right',
      render: (r) => (
        <RowActions>
          <IconButton icon="trash" title="Xóa xe" tone="danger" onClick={() => setRemoving(r)} />
        </RowActions>
      ),
    },
  ];

  return (
    <Card>
      <Toolbar>
        <div className="filters">
          <label className="inline-field">
            <span>Sinh viên</span>
            <Select value={maSV} onChange={(e) => setMaSV(e.target.value)} options={sv.options} placeholder="Tất cả sinh viên" />
          </label>
        </div>
        <button className="btn btn-primary" onClick={() => setCreating(true)}>
          <Icon name="plus" size={16} /> Đăng ký xe
        </button>
      </Toolbar>
      <ErrorBox error={list.error} onRetry={list.reload} />
      <DataTable columns={columns} rows={rows} rowKey={(r) => r.bienSo} loading={list.loading} empty="Chưa có xe nào đăng ký" />
      {creating && (
        <XeForm
          svOptions={sv.options}
          onClose={() => setCreating(false)}
          onSaved={(m) => {
            setCreating(false);
            toast.success(m);
            list.reload();
          }}
        />
      )}
      {removing && (
        <ConfirmModal
          title="Xóa xe"
          message={`Xóa xe ${removing.bienSo}? Xe đã có vé tháng được giữ lại để bảo vệ lịch sử; chỉ xóa xe chưa có dữ liệu liên quan.`}
          onClose={() => setRemoving(null)}
          onConfirm={async () => {
            await api.xe.remove(removing.bienSo);
            setRemoving(null);
            toast.success('Đã xóa xe');
            list.reload();
          }}
        />
      )}
    </Card>
  );
}

function VeForm({ xeOptions, onClose, onSaved }) {
  const { values, bind } = useForm({ bienSo: '', thang: localMonth(), donGiaVe: '' });
  async function submit() {
    await api.veThang.create({
      bienSo: values.bienSo,
      thang: values.thang,
      donGiaVe: values.donGiaVe === '' ? null : Number(values.donGiaVe),
    });
    onSaved('Đã đăng ký vé tháng');
  }
  return (
    <FormModal title="Đăng ký vé tháng" onClose={onClose} onSubmit={submit} submitText="Đăng ký">
      <Field label="Xe" required wide>
        <Select {...bind('bienSo')} options={xeOptions} placeholder="Chọn xe" required />
      </Field>
      <Field label="Tháng" required hint="Chọn tháng đăng ký vé xe">
        <MonthPicker {...bind('thang')} allowClear={false} />
      </Field>
      <Field label="Đơn giá vé (đ)" hint="Cố định 100.000 đồng/xe/tháng">
        <Input type="number" min="0" {...bind('donGiaVe')} />
      </Field>
    </FormModal>
  );
}

function GiaHanForm({ onClose, onSaved }) {
  const next = new Date();
  next.setMonth(next.getMonth() + 1, 1);
  const { values, bind } = useForm({ thang: localMonth(next) });
  async function submit() {
    const r = await api.veThang.giaHan(values.thang);
    onSaved(`Gia hạn vé tháng ${fmtMonth(r.thang)}: tạo mới ${r.soVeDaTao} vé, ${r.soVeDaCoSan} vé đã có sẵn`);
  }
  return (
    <FormModal title="Gia hạn vé tháng hàng loạt" onClose={onClose} onSubmit={submit} submitText="Gia hạn">
      <Field label="Gia hạn cho tháng" required wide hint="Mọi xe có vé tháng trước đó sẽ được đăng ký tiếp, áp dụng đơn giá cố định 100.000 đồng/xe/tháng.">
        <MonthPicker {...bind('thang')} allowClear={false} />
      </Field>
    </FormModal>
  );
}

function VeTab() {
  const toast = useToast();
  const xe = useXeOptions();
  const [bienSo, setBienSo] = useState('');
  const list = useAsync(() => api.veThang.list(bienSo), [bienSo]);
  const [modal, setModal] = useState(null);
  const rows = list.data || [];

  function saved(m) {
    setModal(null);
    toast.success(m);
    list.reload();
  }

  return (
    <Card>
      <Toolbar>
        <div className="filters">
          <label className="inline-field">
            <span>Xe</span>
            <Select value={bienSo} onChange={(e) => setBienSo(e.target.value)} options={xe.options} placeholder="Tất cả xe" />
          </label>
        </div>
        <div className="btn-group">
          <button className="btn btn-ghost" onClick={() => setModal('gia-han')}>
            <Icon name="refresh" size={16} /> Gia hạn hàng loạt
          </button>
          <button className="btn btn-primary" onClick={() => setModal('moi')}>
            <Icon name="plus" size={16} /> Đăng ký vé tháng
          </button>
        </div>
      </Toolbar>
      <ErrorBox error={list.error} onRetry={list.reload} />
      <DataTable
        rows={rows}
        rowKey={(r) => `${r.bienSo}|${r.thang}`}
        loading={list.loading}
        empty="Chưa có vé tháng nào"
        columns={[
          { key: 'bienSo', title: 'Biển số', render: (r) => <b>{r.bienSo}</b> },
          { key: 'thang', title: 'Tháng', render: (r) => fmtMonth(r.thang) },
          { key: 'donGiaVe', title: 'Đơn giá vé', align: 'right', render: (r) => money(r.donGiaVe) },
        ]}
      />
      {modal === 'moi' && <VeForm xeOptions={xe.options} onClose={() => setModal(null)} onSaved={saved} />}
      {modal === 'gia-han' && <GiaHanForm onClose={() => setModal(null)} onSaved={saved} />}
    </Card>
  );
}

function GuiForm({ xeOptions, onClose, onSaved }) {
  const { values, bind } = useForm({ bienSo: '', thoiGianGui: localDateTimeInput() });
  async function submit() {
    await api.luotXe.gui({
      bienSo: values.bienSo,
      thoiGianGui: withSeconds(values.thoiGianGui),
    });
    onSaved('Đã ghi nhận lượt gửi xe');
  }
  return (
    <FormModal title="Ghi nhận gửi xe" onClose={onClose} onSubmit={submit} submitText="Ghi nhận">
      <Field label="Xe" required wide hint="Xe phải có vé tháng của tháng gửi. Mỗi ngày 2 lượt đầu miễn phí, từ lượt thứ 3 tính 3.000 đ.">
        <Select {...bind('bienSo')} options={xeOptions} placeholder="Chọn xe" required />
      </Field>
      <Field label="Gửi lúc" required wide>
        <DateTimePicker {...bind('thoiGianGui')} required />
      </Field>
    </FormModal>
  );
}

function LayForm({ row, onClose, onSaved }) {
  const { values, bind } = useForm({ thoiGianLay: localDateTimeInput() });
  async function submit() {
    await api.luotXe.lay({ bienSo: row.bienSo, thoiGianGui: row.thoiGianGui, thoiGianLay: withSeconds(values.thoiGianLay) });
    onSaved('Đã ghi nhận lấy xe');
  }
  return (
    <FormModal title={`Lấy xe ${row.bienSo}`} onClose={onClose} onSubmit={submit} submitText="Xác nhận lấy xe">
      <p className="field-wide muted">Xe gửi lúc {fmtDateTime(row.thoiGianGui)}</p>
      <Field label="Lấy lúc" required wide>
        <DateTimePicker {...bind('thoiGianLay')} required />
      </Field>
    </FormModal>
  );
}

function LuotTab() {
  const toast = useToast();
  const xe = useXeOptions();
  const [bienSo, setBienSo] = useState('');
  const [ngay, setNgay] = useState(localDate());
  const list = useAsync(() => api.luotXe.list({ bienSo, ngay }), [bienSo, ngay]);
  const dangGui = useAsync(() => api.luotXe.dangGui(), []);
  const [modal, setModal] = useState(null);
  const rows = list.data || [];

  function saved(m) {
    setModal(null);
    toast.success(m);
    list.reload();
    dangGui.reload();
  }

  const layButton = (r) => (
    <button className="btn btn-ghost btn-sm" onClick={() => setModal({ lay: r })}>
      Lấy xe
    </button>
  );

  return (
    <>
      <Card title="Xe đang gửi trong KTX" actions={<Badge tone={dangGui.data?.length ? 'amber' : 'green'}>{dangGui.data ? dangGui.data.length : 0}</Badge>}>
        <ErrorBox error={dangGui.error} onRetry={dangGui.reload} />
        <DataTable
          rows={dangGui.data || []}
          rowKey={(r) => `${r.bienSo}|${r.thoiGianGui}`}
          loading={dangGui.loading}
          empty="Hiện không có xe nào đang gửi"
          columns={[
            { key: 'bienSo', title: 'Biển số', render: (r) => <b>{r.bienSo}</b> },
            { key: 'thoiGianGui', title: 'Gửi lúc', render: (r) => fmtDateTime(r.thoiGianGui) },
            { key: 'soTienPhaiTra', title: 'Phí phát sinh', align: 'right', render: (r) => (Number(r.soTienPhaiTra) > 0 ? money(r.soTienPhaiTra) : 'Miễn phí') },
            { key: 'actions', title: '', align: 'right', render: layButton },
          ]}
        />
      </Card>

      <Card title="Lịch sử gửi/lấy xe" className="mt">
        <Toolbar>
          <div className="filters">
            <label className="inline-field">
              <span>Xe</span>
              <Select value={bienSo} onChange={(e) => setBienSo(e.target.value)} options={xe.options} placeholder="Tất cả xe" />
            </label>
            <div className="inline-field">
              <span>Ngày</span>
              <DatePicker value={ngay} onChange={(e) => setNgay(e.target.value)} />
            </div>
            {ngay && (
              <button className="btn btn-ghost btn-sm" onClick={() => setNgay('')}>
                Xem mọi ngày
              </button>
            )}
          </div>
          <button className="btn btn-primary" onClick={() => setModal('gui')}>
            <Icon name="plus" size={16} /> Ghi nhận gửi xe
          </button>
        </Toolbar>
        <ErrorBox error={list.error} onRetry={list.reload} />
        <DataTable
          rows={rows}
          rowKey={(r) => `${r.bienSo}|${r.thoiGianGui}`}
          loading={list.loading}
          empty="Không có lượt gửi/lấy xe nào"
          columns={[
            { key: 'bienSo', title: 'Biển số', render: (r) => <b>{r.bienSo}</b> },
            { key: 'thoiGianGui', title: 'Gửi lúc', render: (r) => fmtDateTime(r.thoiGianGui) },
            { key: 'thoiGianLay', title: 'Lấy lúc', render: (r) => (r.thoiGianLay ? fmtDateTime(r.thoiGianLay) : <Badge tone="amber">{new Date(r.thoiGianGui) > new Date() ? 'Chưa đến giờ gửi' : 'Đang gửi'}</Badge>) },
            {
              key: 'soTienPhaiTra',
              title: 'Tiền phải trả',
              align: 'right',
              render: (r) => (Number(r.soTienPhaiTra) > 0 ? <b>{money(r.soTienPhaiTra)}</b> : <span className="muted">Miễn phí</span>),
            },
            { key: 'actions', title: '', align: 'right', render: (r) => (r.thoiGianLay ? null : layButton(r)) },
          ]}
        />
      </Card>

      {modal === 'gui' && <GuiForm xeOptions={xe.options} onClose={() => setModal(null)} onSaved={saved} />}
      {modal && modal.lay && <LayForm row={modal.lay} onClose={() => setModal(null)} onSaved={saved} />}
    </>
  );
}

export default function GuiXe() {
  const [tab, setTab] = useState('xe');
  return (
    <>
      <PageHeader title="Gửi xe tháng" subtitle="Đăng ký xe, vé tháng và ghi nhận lượt gửi/lấy xe" />
      <Tabs
        tabs={[
          { key: 'xe', label: 'Xe đăng ký' },
          { key: 've', label: 'Vé tháng' },
          { key: 'luot', label: 'Lượt gửi/lấy xe' },
        ]}
        value={tab}
        onChange={setTab}
      />
      {tab === 'xe' && <XeTab />}
      {tab === 've' && <VeTab />}
      {tab === 'luot' && <LuotTab />}
    </>
  );
}

import { useState } from 'react';
import { api } from '../api/endpoints.js';
import DataTable, { IconButton, RowActions } from '../components/DataTable.jsx';
import { DatePicker, DateTimePicker, Field, Input, Select } from '../components/Fields.jsx';
import Icon from '../components/Icon.jsx';
import { ConfirmModal, FormModal } from '../components/Modal.jsx';
import { useToast } from '../components/Toast.jsx';
import { Card, ErrorBox, PageHeader, Tabs, Toolbar } from '../components/Ui.jsx';
import { useAsync } from '../hooks/useAsync.js';
import { useForm } from '../hooks/useForm.js';
import { useDichVuOptions, useSinhVienOptions } from '../hooks/useOptions.js';
import { fmtDateTime, localDateTimeInput, money, sum, withSeconds } from '../utils/format.js';

function DichVuForm({ initial, onClose, onSaved }) {
  const editing = Boolean(initial);
  const { values, bind } = useForm({
    maDV: initial?.maDV ?? '',
    tenDV: initial?.tenDV ?? '',
    donGia: initial?.donGia ?? '',
  });

  async function submit() {
    const body = { maDV: values.maDV.trim(), tenDV: values.tenDV.trim(), donGia: Number(values.donGia) };
    if (editing) await api.dichVu.update(initial.maDV, body);
    else await api.dichVu.create(body);
    onSaved(editing ? 'Đã cập nhật dịch vụ' : 'Đã thêm dịch vụ');
  }

  return (
    <FormModal title={editing ? `Sửa dịch vụ ${initial.maDV}` : 'Thêm dịch vụ'} onClose={onClose} onSubmit={submit}>
      <Field label="Mã dịch vụ" required>
        <Input {...bind('maDV')} required maxLength={10} disabled={editing} placeholder="VD: DV06" />
      </Field>
      <Field label="Đơn giá (đ)" required hint="Giá cho một đơn vị (kg, suất, giờ, lượt...)">
        <Input type="number" min="0" {...bind('donGia')} required />
      </Field>
      <Field label="Tên dịch vụ" required wide hint="Nên ghi kèm đơn vị, ví dụ: Giặt là(kg). Dịch vụ trông xe dùng đúng tên “Trông xe” và được tổng hợp qua vé/lượt xe.">
        <Input {...bind('tenDV')} required maxLength={50} />
      </Field>
    </FormModal>
  );
}

function DanhMucTab() {
  const toast = useToast();
  const list = useAsync(() => api.dichVu.list(), []);
  const [form, setForm] = useState(null);
  const [removing, setRemoving] = useState(null);
  const rows = list.data || [];

  const columns = [
    { key: 'maDV', title: 'Mã DV', render: (r) => <b>{r.maDV}</b> },
    { key: 'tenDV', title: 'Tên dịch vụ' },
    { key: 'donGia', title: 'Đơn giá', align: 'right', render: (r) => money(r.donGia) },
    {
      key: 'actions',
      title: '',
      align: 'right',
      render: (r) => (
        <RowActions>
          <IconButton icon="edit" title="Sửa" onClick={() => setForm(r)} />
          <IconButton icon="trash" title="Xóa" tone="danger" onClick={() => setRemoving(r)} />
        </RowActions>
      ),
    },
  ];

  return (
    <Card>
      <Toolbar>
        <span className="muted">{rows.length} dịch vụ</span>
        <button className="btn btn-primary" onClick={() => setForm('new')}>
          <Icon name="plus" size={16} /> Thêm dịch vụ
        </button>
      </Toolbar>
      <ErrorBox error={list.error} onRetry={list.reload} />
      <DataTable columns={columns} rows={rows} rowKey={(r) => r.maDV} loading={list.loading} empty="Chưa có dịch vụ nào" />
      {form && (
        <DichVuForm
          initial={form === 'new' ? null : form}
          onClose={() => setForm(null)}
          onSaved={(m) => {
            setForm(null);
            toast.success(m);
            list.reload();
          }}
        />
      )}
      {removing && (
        <ConfirmModal
          title="Xóa dịch vụ"
          message={`Bạn có chắc muốn xóa dịch vụ ${removing.tenDV}?`}
          onClose={() => setRemoving(null)}
          onConfirm={async () => {
            await api.dichVu.remove(removing.maDV);
            setRemoving(null);
            toast.success('Đã xóa dịch vụ');
            list.reload();
          }}
        />
      )}
    </Card>
  );
}

function SuDungForm({ svOptions, dvOptions, onClose, onSaved }) {
  const { values, bind } = useForm({ maSV: '', maDV: '', thoiGianSD: localDateTimeInput(), soLuong: 1 });
  async function submit() {
    await api.suDung.create({
      maSV: values.maSV,
      maDV: values.maDV,
      thoiGianSD: withSeconds(values.thoiGianSD),
      soLuong: Number(values.soLuong),
    });
    onSaved('Đã ghi nhận sử dụng dịch vụ');
  }
  return (
    <FormModal title="Ghi nhận sử dụng dịch vụ" onClose={onClose} onSubmit={submit} submitText="Ghi nhận">
      <Field label="Sinh viên" required wide hint="Chỉ sinh viên đang ở trong KTX mới được sử dụng dịch vụ">
        <Select {...bind('maSV')} options={svOptions} placeholder="Chọn sinh viên" required />
      </Field>
      <Field label="Dịch vụ" required wide>
        <Select {...bind('maDV')} options={dvOptions} placeholder="Chọn dịch vụ" required />
      </Field>
      <Field label="Thời gian sử dụng" required>
        <DateTimePicker {...bind('thoiGianSD')} required />
      </Field>
      <Field label="Số lượng" required>
        <Input type="number" min="1" {...bind('soLuong')} required />
      </Field>
    </FormModal>
  );
}

function SuDungTab() {
  const toast = useToast();
  const sv = useSinhVienOptions();
  const dv = useDichVuOptions();
  const [maSV, setMaSV] = useState('');
  const [tuNgay, setTuNgay] = useState('');
  const [denNgay, setDenNgay] = useState('');
  const list = useAsync(() => api.suDung.list({ maSV, tuNgay, denNgay }), [maSV, tuNgay, denNgay]);
  const [creating, setCreating] = useState(false);
  const [removing, setRemoving] = useState(null);
  const rows = list.data || [];

  const columns = [
    { key: 'thoiGianSD', title: 'Thời gian', render: (r) => fmtDateTime(r.thoiGianSD) },
    { key: 'maSV', title: 'Mã SV', render: (r) => <b>{r.maSV}</b> },
    { key: 'hoTen', title: 'Họ và tên' },
    { key: 'tenDV', title: 'Dịch vụ' },
    { key: 'soLuong', title: 'Số lượng', align: 'right' },
    { key: 'donGia', title: 'Đơn giá', align: 'right', render: (r) => money(r.donGia) },
    { key: 'thanhTien', title: 'Thành tiền', align: 'right', render: (r) => <b>{money(r.thanhTien)}</b> },
    {
      key: 'actions',
      title: '',
      align: 'right',
      render: (r) => (
        <RowActions>
          <IconButton icon="trash" title="Hủy ghi nhận" tone="danger" onClick={() => setRemoving(r)} />
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
          <div className="inline-field">
            <span>Từ ngày</span>
            <DatePicker value={tuNgay} onChange={(e) => setTuNgay(e.target.value)} />
          </div>
          <div className="inline-field">
            <span>Đến ngày</span>
            <DatePicker value={denNgay} onChange={(e) => setDenNgay(e.target.value)} />
          </div>
        </div>
        <button className="btn btn-primary" onClick={() => setCreating(true)}>
          <Icon name="plus" size={16} /> Ghi nhận sử dụng
        </button>
      </Toolbar>
      <ErrorBox error={list.error} onRetry={list.reload} />
      <DataTable
        columns={columns}
        rows={rows}
        rowKey={(r) => `${r.maSV}|${r.maDV}|${r.thoiGianSD}`}
        loading={list.loading}
        empty="Chưa có lần sử dụng dịch vụ nào"
        foot={
          rows.length > 0 && (
            <tr>
              <td colSpan={6}>
                <b>Tổng cộng ({rows.length} lần)</b>
              </td>
              <td className="ta-right">
                <b>{money(sum(rows, (r) => r.thanhTien))}</b>
              </td>
              <td />
            </tr>
          )
        }
      />
      {creating && (
        <SuDungForm
          svOptions={sv.options}
          dvOptions={dv.options}
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
          title="Hủy ghi nhận sử dụng dịch vụ"
          message={`Hủy lần sử dụng ${removing.tenDV} của ${removing.hoTen} lúc ${fmtDateTime(removing.thoiGianSD)}?`}
          confirmText="Hủy ghi nhận"
          onClose={() => setRemoving(null)}
          onConfirm={async () => {
            await api.suDung.remove({ maSV: removing.maSV, maDV: removing.maDV, thoiGianSD: removing.thoiGianSD });
            setRemoving(null);
            toast.success('Đã hủy ghi nhận');
            list.reload();
          }}
        />
      )}
    </Card>
  );
}

export default function DichVu() {
  const [tab, setTab] = useState('danh-muc');
  return (
    <>
      <PageHeader title="Quản lý Dịch vụ" subtitle="Danh mục dịch vụ và ghi nhận sinh viên sử dụng dịch vụ" />
      <Tabs
        tabs={[
          { key: 'danh-muc', label: 'Danh mục dịch vụ' },
          { key: 'su-dung', label: 'Ghi nhận sử dụng' },
        ]}
        value={tab}
        onChange={setTab}
      />
      {tab === 'danh-muc' ? <DanhMucTab /> : <SuDungTab />}
    </>
  );
}

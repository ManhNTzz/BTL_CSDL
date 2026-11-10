import { useState } from 'react';
import { api } from '../api/endpoints.js';
import DataTable, { IconButton, RowActions } from '../components/DataTable.jsx';
import { DatePicker, Field, Input } from '../components/Fields.jsx';
import HoSoModal from '../components/HoSoModal.jsx';
import Icon from '../components/Icon.jsx';
import { ConfirmModal, FormModal } from '../components/Modal.jsx';
import { useToast } from '../components/Toast.jsx';
import { Card, ErrorBox, PageHeader, Toolbar } from '../components/Ui.jsx';
import { useAsync } from '../hooks/useAsync.js';
import { useForm } from '../hooks/useForm.js';
import { fmtDate } from '../utils/format.js';

function SinhVienForm({ initial, onClose, onSaved }) {
  const editing = Boolean(initial);
  const { values, bind } = useForm({
    maSV: initial?.maSV ?? '',
    cccd: initial?.cccd ?? '',
    hoTen: initial?.hoTen ?? '',
    ngaySinh: initial?.ngaySinh ?? '',
    lop: initial?.lop ?? '',
    queQuan: initial?.queQuan ?? '',
  });

  async function submit() {
    const body = {
      hoTen: values.hoTen.trim(),
      ngaySinh: values.ngaySinh,
      lop: values.lop.trim(),
      queQuan: values.queQuan.trim(),
    };
    if (editing) {
      await api.sinhVien.update(initial.maSV, body);
    } else {
      await api.sinhVien.create({ ...body, maSV: values.maSV.trim(), cccd: values.cccd.trim() });
    }
    onSaved(editing ? 'Đã cập nhật sinh viên' : 'Đã thêm sinh viên');
  }

  return (
    <FormModal title={editing ? `Sửa sinh viên ${initial.maSV}` : 'Thêm sinh viên'} onClose={onClose} onSubmit={submit}>
      <Field label="Mã sinh viên" required>
        <Input {...bind('maSV')} required maxLength={10} disabled={editing} placeholder="VD: SV009" />
      </Field>
      <Field label="Số CCCD" required hint="Gồm đúng 12 chữ số">
        <Input {...bind('cccd')} required pattern="\d{12}" maxLength={12} inputMode="numeric" disabled={editing} />
      </Field>
      <Field label="Họ và tên" required wide>
        <Input {...bind('hoTen')} required maxLength={100} />
      </Field>
      <Field label="Ngày sinh" required>
        <DatePicker {...bind('ngaySinh')} required />
      </Field>
      <Field label="Lớp" required>
        <Input {...bind('lop')} required maxLength={20} placeholder="VD: CNTT-K68" />
      </Field>
      <Field label="Quê quán" required wide>
        <Input {...bind('queQuan')} required maxLength={100} />
      </Field>
    </FormModal>
  );
}

export default function SinhVien() {
  const toast = useToast();
  const [keyword, setKeyword] = useState('');
  const [applied, setApplied] = useState('');
  const list = useAsync(() => api.sinhVien.list(applied), [applied]);
  const [form, setForm] = useState(null); // 'new' | sinh viên đang sửa
  const [profile, setProfile] = useState(null);
  const [removing, setRemoving] = useState(null);
  const rows = list.data || [];

  function saved(message) {
    setForm(null);
    toast.success(message);
    list.reload();
  }

  const columns = [
    { key: 'maSV', title: 'Mã SV', render: (r) => <b>{r.maSV}</b> },
    { key: 'hoTen', title: 'Họ và tên' },
    { key: 'ngaySinh', title: 'Ngày sinh', render: (r) => fmtDate(r.ngaySinh) },
    { key: 'cccd', title: 'Số CCCD' },
    { key: 'lop', title: 'Lớp' },
    { key: 'queQuan', title: 'Quê quán' },
    {
      key: 'actions',
      title: '',
      align: 'right',
      render: (r) => (
        <RowActions>
          <IconButton icon="eye" title="Xem hồ sơ" onClick={() => setProfile(r.maSV)} />
          <IconButton icon="edit" title="Sửa" onClick={() => setForm(r)} />
          <IconButton icon="trash" title="Xóa" tone="danger" onClick={() => setRemoving(r)} />
        </RowActions>
      ),
    },
  ];

  return (
    <>
      <PageHeader
        title="Quản lý Sinh viên"
        subtitle="Thêm, sửa, xóa, tìm kiếm sinh viên và xem hồ sơ đầy đủ"
        actions={
          <button className="btn btn-primary" onClick={() => setForm('new')}>
            <Icon name="plus" size={16} /> Thêm sinh viên
          </button>
        }
      />
      <Card>
        <Toolbar>
          <form
            className="search-form"
            onSubmit={(e) => {
              e.preventDefault();
              setApplied(keyword.trim());
            }}
          >
            <div className="input-icon">
              <Icon name="search" size={16} />
              <input
                className="input"
                placeholder="Tìm theo mã SV, họ tên, lớp, quê quán..."
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
              />
            </div>
            <button className="btn btn-primary" type="submit">
              Tìm kiếm
            </button>
            {applied && (
              <button
                className="btn btn-ghost"
                type="button"
                onClick={() => {
                  setKeyword('');
                  setApplied('');
                }}
              >
                Xóa bộ lọc
              </button>
            )}
          </form>
          <span className="muted">{rows.length} sinh viên</span>
        </Toolbar>
        <ErrorBox error={list.error} onRetry={list.reload} />
        <DataTable columns={columns} rows={rows} rowKey={(r) => r.maSV} loading={list.loading} empty="Không tìm thấy sinh viên nào" />
      </Card>

      {form && (
        <SinhVienForm initial={form === 'new' ? null : form} onClose={() => setForm(null)} onSaved={saved} />
      )}
      {profile && <HoSoModal maSV={profile} onClose={() => setProfile(null)} />}
      {removing && (
        <ConfirmModal
          title="Xóa sinh viên"
          message={`Bạn có chắc muốn xóa sinh viên ${removing.hoTen} (${removing.maSV})?`}
          onClose={() => setRemoving(null)}
          onConfirm={async () => {
            await api.sinhVien.remove(removing.maSV);
            setRemoving(null);
            toast.success('Đã xóa sinh viên');
            list.reload();
          }}
        />
      )}
    </>
  );
}

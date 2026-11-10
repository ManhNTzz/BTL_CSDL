import { useState } from 'react';
import { api } from '../api/endpoints.js';
import DataTable, { IconButton, RowActions } from '../components/DataTable.jsx';
import { DatePicker, DateTimePicker, Field, Input, Select } from '../components/Fields.jsx';
import Icon from '../components/Icon.jsx';
import { ConfirmModal, FormModal } from '../components/Modal.jsx';
import { useToast } from '../components/Toast.jsx';
import { Badge, Card, ErrorBox, PageHeader, Tabs, Toolbar } from '../components/Ui.jsx';
import { useAsync } from '../hooks/useAsync.js';
import { useForm } from '../hooks/useForm.js';
import { useSinhVienOptions } from '../hooks/useOptions.js';
import { fmtDate, fmtDateTime, localDateTimeInput, withSeconds } from '../utils/format.js';

function LuotThamForm({ svOptions, guests, onClose, onSaved }) {
  const { values, bind } = useForm({
    cccdKhach: '',
    hoTen: '',
    ngaySinh: '',
    maSV: '',
    ngayDenChoi: localDateTimeInput(),
  });
  const found = guests.find((g) => g.cccd === values.cccdKhach.trim());

  async function submit() {
    await api.khach.ghiNhan({
      cccdKhach: values.cccdKhach.trim(),
      hoTen: found ? null : values.hoTen.trim() || null,
      ngaySinh: found ? null : values.ngaySinh || null,
      maSV: values.maSV,
      ngayDenChoi: withSeconds(values.ngayDenChoi),
    });
    onSaved('Đã ghi nhận khách đến thăm');
  }

  return (
    <FormModal title="Ghi nhận khách đến thăm" onClose={onClose} onSubmit={submit} submitText="Ghi nhận">
      <Field label="Số CCCD của khách" required hint={found ? undefined : 'Gồm đúng 12 chữ số'}>
        <Input {...bind('cccdKhach')} required pattern="\d{12}" maxLength={12} inputMode="numeric" />
      </Field>
      <Field label="Sinh viên được thăm" required hint="Sinh viên phải đang ở trong KTX vào ngày khách đến">
        <Select {...bind('maSV')} options={svOptions} placeholder="Chọn sinh viên" required />
      </Field>
      {found ? (
        <div className="field field-wide">
          <Badge tone="green">Khách đã có trong hệ thống</Badge> <b>{found.hoTen}</b>, sinh ngày {fmtDate(found.ngaySinh)}
        </div>
      ) : (
        <>
          <Field label="Họ và tên khách" required={values.cccdKhach.length === 12} hint="Cần nhập nếu là khách mới">
            <Input {...bind('hoTen')} maxLength={100} required={values.cccdKhach.length === 12} />
          </Field>
          <Field label="Ngày sinh khách" required={values.cccdKhach.length === 12}>
            <DatePicker {...bind('ngaySinh')} required={values.cccdKhach.length === 12} />
          </Field>
        </>
      )}
      <Field label="Thời gian đến" required wide>
        <DateTimePicker {...bind('ngayDenChoi')} required />
      </Field>
    </FormModal>
  );
}

function LuotThamTab() {
  const toast = useToast();
  const sv = useSinhVienOptions();
  const guests = useAsync(() => api.khach.list(), []);
  const [maSV, setMaSV] = useState('');
  const [tuNgay, setTuNgay] = useState('');
  const [denNgay, setDenNgay] = useState('');
  const list = useAsync(() => api.khach.luotTham({ maSV, tuNgay, denNgay }), [maSV, tuNgay, denNgay]);
  const [creating, setCreating] = useState(false);
  const [removing, setRemoving] = useState(null);
  const rows = list.data || [];

  const columns = [
    { key: 'ngayDenChoi', title: 'Thời gian đến', render: (r) => fmtDateTime(r.ngayDenChoi) },
    { key: 'hoTenKhach', title: 'Khách', render: (r) => <b>{r.hoTenKhach}</b> },
    { key: 'cccdKhach', title: 'CCCD khách' },
    { key: 'hoTenSV', title: 'Đến thăm sinh viên', render: (r) => `${r.hoTenSV} (${r.maSV})` },
    {
      key: 'actions',
      title: '',
      align: 'right',
      render: (r) => (
        <RowActions>
          <IconButton icon="trash" title="Hủy lượt thăm" tone="danger" onClick={() => setRemoving(r)} />
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
          <Icon name="plus" size={16} /> Ghi nhận khách đến
        </button>
      </Toolbar>
      <ErrorBox error={list.error} onRetry={list.reload} />
      <DataTable columns={columns} rows={rows} rowKey={(r) => `${r.cccdKhach}|${r.maSV}|${r.ngayDenChoi}`} loading={list.loading} empty="Chưa có lượt khách đến thăm nào" />
      {creating && (
        <LuotThamForm
          svOptions={sv.options}
          guests={guests.data || []}
          onClose={() => setCreating(false)}
          onSaved={(m) => {
            setCreating(false);
            toast.success(m);
            list.reload();
            guests.reload();
          }}
        />
      )}
      {removing && (
        <ConfirmModal
          title="Hủy lượt thăm"
          message={`Hủy lượt khách ${removing.hoTenKhach} thăm ${removing.hoTenSV} lúc ${fmtDateTime(removing.ngayDenChoi)}?`}
          confirmText="Hủy lượt thăm"
          onClose={() => setRemoving(null)}
          onConfirm={async () => {
            await api.khach.huyLuot({ cccdKhach: removing.cccdKhach, maSV: removing.maSV, ngayDenChoi: removing.ngayDenChoi });
            setRemoving(null);
            toast.success('Đã hủy lượt thăm');
            list.reload();
          }}
        />
      )}
    </Card>
  );
}

function KhachTab() {
  const guests = useAsync(() => api.khach.list(), []);
  const [keyword, setKeyword] = useState('');
  const k = keyword.trim().toLowerCase();
  const rows = (guests.data || []).filter((g) => !k || g.hoTen.toLowerCase().includes(k) || g.cccd.includes(k));
  return (
    <Card>
      <Toolbar>
        <div className="input-icon search-box">
          <Icon name="search" size={16} />
          <input className="input" placeholder="Tìm theo tên hoặc CCCD khách..." value={keyword} onChange={(e) => setKeyword(e.target.value)} />
        </div>
        <span className="muted">{rows.length} khách</span>
      </Toolbar>
      <ErrorBox error={guests.error} onRetry={guests.reload} />
      <DataTable
        rows={rows}
        rowKey={(r) => r.cccd}
        loading={guests.loading}
        empty="Chưa có khách nào"
        columns={[
          { key: 'hoTen', title: 'Họ và tên', render: (r) => <b>{r.hoTen}</b> },
          { key: 'cccd', title: 'Số CCCD' },
          { key: 'ngaySinh', title: 'Ngày sinh', render: (r) => fmtDate(r.ngaySinh) },
        ]}
      />
    </Card>
  );
}

export default function Khach() {
  const [tab, setTab] = useState('luot');
  return (
    <>
      <PageHeader title="Khách ra vào" subtitle="Ghi nhận khách đến thăm sinh viên và tra cứu lịch sử" />
      <Tabs
        tabs={[
          { key: 'luot', label: 'Lượt khách đến thăm' },
          { key: 'khach', label: 'Danh sách khách' },
        ]}
        value={tab}
        onChange={setTab}
      />
      {tab === 'luot' ? <LuotThamTab /> : <KhachTab />}
    </>
  );
}

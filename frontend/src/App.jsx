import { useEffect, useState } from 'react';
import { api } from './api/endpoints.js';
import Icon from './components/Icon.jsx';
import Modal from './components/Modal.jsx';
import { ToastProvider } from './components/Toast.jsx';
import { useAsync } from './hooks/useAsync.js';
import BaoCao from './pages/BaoCao.jsx';
import Dashboard from './pages/Dashboard.jsx';
import DichVu from './pages/DichVu.jsx';
import GuiXe from './pages/GuiXe.jsx';
import Khach from './pages/Khach.jsx';
import Phong from './pages/Phong.jsx';
import SinhVien from './pages/SinhVien.jsx';

const ROUTES = [
  { path: 'tong-quan', title: 'Tổng quan Dashboard', icon: 'grid', page: Dashboard },
  { path: 'sinh-vien', title: 'Quản lý Sinh viên', icon: 'users', page: SinhVien },
  { path: 'phong', title: 'Quản lý Phòng ở', icon: 'home', page: Phong },
  { path: 'dich-vu', title: 'Quản lý Dịch vụ', icon: 'tag', page: DichVu },
  { path: 'khach', title: 'Khách ra vào', icon: 'userCheck', page: Khach },
  { path: 'gui-xe', title: 'Gửi xe tháng', icon: 'bike', page: GuiXe },
  { path: 'bao-cao', title: 'Hóa đơn và Báo cáo', icon: 'fileText', page: BaoCao },
];

function readRoute() {
  return window.location.hash.replace(/^#\/?/, '') || 'tong-quan';
}

function useRoute() {
  const [route, setRoute] = useState(readRoute);
  useEffect(() => {
    const onChange = () => setRoute(readRoute());
    window.addEventListener('hashchange', onChange);
    return () => window.removeEventListener('hashchange', onChange);
  }, []);
  return route;
}

function Logo() {
  return (
    <svg width="34" height="34" viewBox="0 0 34 34" aria-hidden="true">
      <rect width="34" height="34" rx="9" fill="#ffffff" />
      <path d="M17 5l10 6v12l-10 6-10-6V11z" fill="#1769e0" />
      <path d="M11 20v-6l6-4 6 4v6h-3.4v-4h-5.2v4z" fill="#ffffff" />
      <circle cx="17" cy="23.5" r="1.7" fill="#e5484d" />
    </svg>
  );
}

function Sidebar({ current }) {
  return (
    <aside className="sidebar">
      <div className="brand">
        <Logo />
        <span className="brand-text">Quản lý KTX</span>
      </div>
      <nav className="nav">
        {ROUTES.map((r) => (
          <a key={r.path} href={`#/${r.path}`} className={r.path === current ? 'active' : ''} title={r.title}>
            <Icon name={r.icon} size={18} />
            <span>{r.title}</span>
          </a>
        ))}
      </nav>
    </aside>
  );
}

function AlertBell() {
  const [open, setOpen] = useState(false);
  const res = useAsync(() => api.canhBao(), []);
  const c = res.data;
  const groups = c
    ? [
        { n: c.xeChuaCoVeThangNay.length, text: 'xe chưa có vé tháng này', to: 'gui-xe' },
        { n: c.phongDayNguoi.length, text: 'phòng đã đầy', to: 'phong' },
        { n: c.sinhVienChuaCoPhong.length, text: 'sinh viên chưa có phòng', to: 'phong' },
        { n: c.sapHetHanO.length, text: 'sinh viên sắp hết hạn ở trong 30 ngày', to: 'phong' },
        { n: c.xeGuiTuNgayTruoc.length, text: 'xe gửi từ hôm trước chưa lấy', to: 'gui-xe' },
      ].filter((g) => g.n > 0)
    : [];
  const total = groups.reduce((s, g) => s + g.n, 0);

  return (
    <div className="bell-wrap">
      <button
        className="icon-btn"
        aria-label="Thông báo"
        onClick={() => {
          setOpen((o) => !o);
          res.reload();
        }}
      >
        <Icon name="bell" size={19} />
        {total > 0 && <span className="bell-count">{total > 99 ? '99+' : total}</span>}
      </button>
      {open && (
        <div className="dropdown" onMouseLeave={() => setOpen(false)}>
          <div className="dropdown-head">Thông báo</div>
          {groups.length === 0 ? (
            <p className="muted small dropdown-empty">Không có việc cần chú ý.</p>
          ) : (
            groups.map((g) => (
              <a key={g.text} href={`#/${g.to}`} onClick={() => setOpen(false)}>
                <b>{g.n}</b> {g.text}
              </a>
            ))
          )}
          <a href="#/tong-quan" className="dropdown-foot" onClick={() => setOpen(false)}>
            Xem chi tiết ở Tổng quan
          </a>
        </div>
      )}
    </div>
  );
}

function HelpModal({ onClose }) {
  return (
    <Modal title="Hướng dẫn sử dụng nhanh" onClose={onClose} size="lg">
      <ol className="guide">
        <li><b>Thêm sinh viên</b> ở mục Quản lý Sinh viên.</li>
        <li><b>Đăng ký ở phòng</b> ở mục Quản lý Phòng ở. Hệ thống không cho vượt số người tối đa của phòng.</li>
        <li><b>Ghi nhận dịch vụ</b> sinh viên đã dùng (giặt là, ăn uống...) ở mục Quản lý Dịch vụ.</li>
        <li><b>Ghi nhận khách đến thăm</b> ở mục Khách ra vào.</li>
        <li>
          <b>Gửi xe tháng:</b> đăng ký xe (tối đa 2 xe mỗi sinh viên), đăng ký vé tháng rồi ghi nhận các lượt gửi/lấy xe.
          Mỗi ngày 2 lượt đầu miễn phí, từ lượt thứ 3 tính 3.000 đ.
        </li>
        <li><b>Cuối tháng:</b> vào Hóa đơn và Báo cáo để xem, in hóa đơn từng sinh viên hoặc xuất file Excel.</li>
      </ol>
      <p className="muted small">Chỉ sinh viên đang ở trong KTX mới được dùng dịch vụ, gửi xe và tiếp khách.</p>
    </Modal>
  );
}

function Topbar({ onToggle }) {
  const [help, setHelp] = useState(false);
  return (
    <header className="topbar">
      <button className="icon-btn" onClick={onToggle} aria-label="Thu gọn menu">
        <Icon name="menu" size={20} />
      </button>
      <div className="spacer" />
      <AlertBell />
      <button className="icon-btn" aria-label="Hướng dẫn" onClick={() => setHelp(true)}>
        <Icon name="help" size={19} />
      </button>
      <div className="avatar" title="Quản lý KTX">
        QL
      </div>
      {help && <HelpModal onClose={() => setHelp(false)} />}
    </header>
  );
}

export default function App() {
  const route = useRoute();
  const [collapsed, setCollapsed] = useState(false);
  const current = ROUTES.find((r) => r.path === route) || ROUTES[0];
  const Page = current.page;

  return (
    <ToastProvider>
      <div className={`app${collapsed ? ' is-collapsed' : ''}`}>
        <Sidebar current={current.path} />
        <div className="main">
          <Topbar onToggle={() => setCollapsed((c) => !c)} />
          <main className="content">
            <Page key={current.path} />
          </main>
        </div>
      </div>
    </ToastProvider>
  );
}

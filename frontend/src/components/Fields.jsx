import { useEffect, useRef, useState } from 'react';
import { createPortal } from 'react-dom';
import Icon from './Icon.jsx';

export function Field({ label, hint, required, wide, children }) {
  return (
    <div className={`field${wide ? ' field-wide' : ''}`}>
      <span className="field-label">
        {label}
        {required && <i className="req"> *</i>}
      </span>
      {children}
      {hint && <small className="field-hint">{hint}</small>}
    </div>
  );
}

export function Input(props) {
  return <input className="input" {...props} />;
}

/** options: [{value, label}] */
export function Select({ options, placeholder, ...rest }) {
  return (
    <select className="input" {...rest}>
      {placeholder !== undefined && <option value="">{placeholder}</option>}
      {options.map((o) => (
        <option key={o.value} value={o.value}>
          {o.label}
        </option>
      ))}
    </select>
  );
}

const VI_MONTHS = [
  'Tháng 1',
  'Tháng 2',
  'Tháng 3',
  'Tháng 4',
  'Tháng 5',
  'Tháng 6',
  'Tháng 7',
  'Tháng 8',
  'Tháng 9',
  'Tháng 10',
  'Tháng 11',
  'Tháng 12',
];

const pad = (n) => String(n).padStart(2, '0');

function parseDate(v) {
  if (v && /^\d{4}-\d{2}-\d{2}/.test(v)) {
    const [y, m, d] = v.slice(0, 10).split('-').map(Number);
    return { year: y, month: m - 1, day: d };
  }
  const now = new Date();
  return { year: now.getFullYear(), month: now.getMonth(), day: now.getDate() };
}

function parseDateTime(v) {
  if (v && /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/.test(v)) {
    const dPart = v.slice(0, 10);
    const tPart = v.slice(11, 16);
    const [y, m, d] = dPart.split('-').map(Number);
    const [h, min] = tPart.split(':').map(Number);
    return {
      year: y,
      month: m - 1,
      day: d,
      dateStr: dPart,
      hour: pad(h),
      minute: pad(min),
    };
  }
  const now = new Date();
  const y = now.getFullYear();
  const m = now.getMonth();
  const d = now.getDate();
  const dStr = `${y}-${pad(m + 1)}-${pad(d)}`;
  return {
    year: y,
    month: m,
    day: d,
    dateStr: dStr,
    hour: pad(now.getHours()),
    minute: pad(now.getMinutes()),
  };
}

function getCalendarGrid(year, month, selectedDateStr, min, max) {
  const firstDay = new Date(year, month, 1).getDay();
  const startOffset = (firstDay + 6) % 7;
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const daysInPrev = new Date(year, month, 0).getDate();
  const now = new Date();
  const todayStr = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;

  const cells = [];

  const prevY = month === 0 ? year - 1 : year;
  const prevM = month === 0 ? 12 : month;
  for (let i = startOffset - 1; i >= 0; i--) {
    const day = daysInPrev - i;
    const dateStr = `${prevY}-${pad(prevM)}-${pad(day)}`;
    cells.push({
      dateStr,
      dayNumber: day,
      isCurrentMonth: false,
      isSelected: dateStr === selectedDateStr,
      isToday: dateStr === todayStr,
      disabled: (min && dateStr < min) || (max && dateStr > max),
    });
  }

  for (let day = 1; day <= daysInMonth; day++) {
    const dateStr = `${year}-${pad(month + 1)}-${pad(day)}`;
    cells.push({
      dateStr,
      dayNumber: day,
      isCurrentMonth: true,
      isSelected: dateStr === selectedDateStr,
      isToday: dateStr === todayStr,
      disabled: (min && dateStr < min) || (max && dateStr > max),
    });
  }

  const remaining = (7 - (cells.length % 7)) % 7;
  const nextY = month === 11 ? year + 1 : year;
  const nextM = month === 11 ? 1 : month + 2;
  for (let day = 1; day <= remaining; day++) {
    const dateStr = `${nextY}-${pad(nextM)}-${pad(day)}`;
    cells.push({
      dateStr,
      dayNumber: day,
      isCurrentMonth: false,
      isSelected: dateStr === selectedDateStr,
      isToday: dateStr === todayStr,
      disabled: (min && dateStr < min) || (max && dateStr > max),
    });
  }

  return cells;
}

function getYearOptions(centerYear) {
  const years = [];
  const minYear = Math.min(1950, centerYear - 10);
  const maxYear = Math.max(2040, centerYear + 10);
  for (let y = minYear; y <= maxYear; y++) {
    years.push(y);
  }
  return years;
}

function useDropdownPosition(open, wrapRef, dropdownRef, defaultHeight = 330, defaultWidth = 290) {
  const [coords, setCoords] = useState({ top: -9999, left: -9999 });

  useEffect(() => {
    if (!open) return;

    const updatePos = () => {
      if (!wrapRef.current) return;
      const rect = wrapRef.current.getBoundingClientRect();
      const h = dropdownRef.current?.offsetHeight || defaultHeight;
      const w = dropdownRef.current?.offsetWidth || defaultWidth;

      const spaceBelow = window.innerHeight - rect.bottom;
      const spaceAbove = rect.top;

      // Drop up only if space below cannot fit the dropdown (< h + 10) AND space above can fit it
      const shouldDropUp = spaceBelow < h + 10 && spaceAbove > h + 10;

      let top = shouldDropUp ? rect.top - h - 4 : rect.bottom + 4;
      let left = rect.left;

      if (left + w > window.innerWidth - 10) {
        left = Math.max(10, window.innerWidth - w - 10);
      }
      if (top < 8) top = 8;
      if (top + h > window.innerHeight - 8) {
        top = Math.max(8, window.innerHeight - h - 8);
      }

      setCoords({ top, left });
    };

    updatePos();
    const raf = requestAnimationFrame(updatePos);
    window.addEventListener('resize', updatePos);
    window.addEventListener('scroll', updatePos, true);

    return () => {
      cancelAnimationFrame(raf);
      window.removeEventListener('resize', updatePos);
      window.removeEventListener('scroll', updatePos, true);
    };
  }, [open, defaultHeight, defaultWidth]);

  return coords;
}

/**
 * Bộ chọn Tháng hoàn toàn bằng Tiếng Việt (thay thế thẻ input type="month" mặc định của trình duyệt)
 * Trả về giá trị định dạng: yyyy-MM (Ví dụ: 2026-11)
 */
export function MonthPicker({
  value = '',
  onChange,
  placeholder = 'Chọn tháng...',
  disabled = false,
  allowClear = true,
  className = '',
  name,
}) {
  const [open, setOpen] = useState(false);
  const wrapRef = useRef(null);
  const dropdownRef = useRef(null);

  const getYearFromValue = (v) => {
    return v && /^\d{4}-\d{2}$/.test(v)
      ? parseInt(v.slice(0, 4), 10)
      : new Date().getFullYear();
  };

  const [viewYear, setViewYear] = useState(() => getYearFromValue(value));
  const coords = useDropdownPosition(open, wrapRef, dropdownRef, 270, 250);

  // Khi mở bảng popup hoặc khi giá trị value bên ngoài thay đổi: luôn đồng bộ năm hiển thị
  useEffect(() => {
    if (open) {
      setViewYear(getYearFromValue(value));
    }
  }, [open, value]);

  useEffect(() => {
    if (!open) return;
    const handleOutside = (e) => {
      if (
        wrapRef.current &&
        !wrapRef.current.contains(e.target) &&
        dropdownRef.current &&
        !dropdownRef.current.contains(e.target)
      ) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handleOutside);
    return () => document.removeEventListener('mousedown', handleOutside);
  }, [open]);

  const emit = (val) => {
    if (disabled) return;
    if (typeof onChange === 'function') {
      onChange({ target: { value: val, name } });
    }
  };

  const selectMonth = (mIndex) => {
    const mm = String(mIndex + 1).padStart(2, '0');
    emit(`${viewYear}-${mm}`);
    setOpen(false);
  };

  const selectCurrentMonth = (e) => {
    e.preventDefault();
    e.stopPropagation();
    const now = new Date();
    const yyyy = now.getFullYear();
    const mm = String(now.getMonth() + 1).padStart(2, '0');
    setViewYear(yyyy);
    emit(`${yyyy}-${mm}`);
    setOpen(false);
  };

  const clearValue = (e) => {
    e.preventDefault();
    e.stopPropagation();
    emit('');
    setOpen(false);
  };

  let displayText = '';
  if (value && /^\d{4}-\d{2}$/.test(value)) {
    const [y, m] = value.split('-');
    displayText = `Tháng ${parseInt(m, 10)}/${y}`;
  }

  const now = new Date();
  const currentY = now.getFullYear();
  const currentM = now.getMonth();

  const selectedY = value && /^\d{4}-\d{2}$/.test(value) ? parseInt(value.slice(0, 4), 10) : null;
  const selectedM = value && /^\d{4}-\d{2}$/.test(value) ? parseInt(value.slice(5, 7), 10) - 1 : null;

  // Danh sách các năm để chọn nhanh trực tiếp
  const years = [];
  const minYear = Math.min(2020, viewYear - 5);
  const maxYear = Math.max(2035, viewYear + 5);
  for (let y = minYear; y <= maxYear; y++) {
    years.push(y);
  }

  return (
    <div className={`month-picker-wrap ${className}`} ref={wrapRef}>
      <div
        role="button" aria-expanded={open} aria-haspopup="dialog"
        className={`month-picker-input ${open ? 'is-open' : ''} ${disabled ? 'is-disabled' : ''}`}
        onClick={() => !disabled && setOpen((o) => !o)}
        tabIndex={disabled ? -1 : 0}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            if (!disabled) setOpen((o) => !o);
          } else if (e.key === 'Escape') {
            setOpen(false);
          }
        }}
      >
        <span className={`month-picker-value ${!displayText ? 'is-placeholder' : ''}`}>
          {displayText || placeholder}
        </span>
        <div className="month-picker-icons">
          {allowClear && value && !disabled && (
            <span
              className="month-picker-clear"
              title="Xóa chọn tháng"
              onClick={clearValue}
            >
              ✕
            </span>
          )}
          <Icon name="calendar" size={15} />
        </div>
      </div>

      {open &&
        createPortal(
          <div
            ref={dropdownRef}
            className="month-picker-dropdown"
            style={{
              position: 'fixed',
              top: `${coords.top}px`,
              left: `${coords.left}px`,
              zIndex: 99999,
            }}
            onMouseDown={(e) => e.stopPropagation()}
            onClick={(e) => e.stopPropagation()}
          >
            <div className="month-picker-header">
            <button
              type="button"
              className="month-picker-nav-btn"
              title="Năm trước"
              onClick={(e) => {
                e.preventDefault();
                e.stopPropagation();
                setViewYear((y) => y - 1);
              }}
            >
              ‹
            </button>

            <div className="month-picker-year-selector">
              <select
                className="month-picker-year-select"
                value={viewYear}
                onChange={(e) => {
                  e.stopPropagation();
                  setViewYear(parseInt(e.target.value, 10));
                }}
                onClick={(e) => e.stopPropagation()}
              >
                {years.map((y) => (
                  <option key={y} value={y}>
                    Năm {y}
                  </option>
                ))}
              </select>
            </div>

            <button
              type="button"
              className="month-picker-nav-btn"
              title="Năm sau"
              onClick={(e) => {
                e.preventDefault();
                e.stopPropagation();
                setViewYear((y) => y + 1);
              }}
            >
              ›
            </button>
          </div>

          <div className="month-picker-grid">
            {Array.from({ length: 12 }, (_, i) => {
              const isSelected = selectedY === viewYear && selectedM === i;
              const isCurrent = currentY === viewYear && currentM === i;
              return (
                <button
                  key={i}
                  type="button"
                  className={`month-picker-cell ${isSelected ? 'is-selected' : ''} ${
                    isCurrent ? 'is-current' : ''
                  }`}
                  onClick={(e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    selectMonth(i);
                  }}
                >
                  Tháng {i + 1}
                </button>
              );
            })}
          </div>

          <div className="month-picker-footer">
            <button
              type="button"
              className="month-picker-btn-link"
              onClick={selectCurrentMonth}
            >
              Tháng này
            </button>
            {allowClear && value && (
              <button
                type="button"
                className="month-picker-btn-link danger"
                onClick={clearValue}
              >
                Xóa
              </button>
            )}
          </div>
        </div>,
        document.body
      )}
    </div>
  );
}

/**
 * Bộ chọn Ngày hoàn toàn bằng Tiếng Việt (thay thế input type="date")
 * Giá trị trả về: yyyy-MM-dd
 */
export function DatePicker({
  value = '',
  onChange,
  placeholder = 'Chọn ngày...',
  disabled = false,
  required = false,
  allowClear = true,
  className = '',
  name,
  min,
  max,
}) {
  const [open, setOpen] = useState(false);
  const wrapRef = useRef(null);
  const dropdownRef = useRef(null);

  const init = parseDate(value);
  const [viewYear, setViewYear] = useState(init.year);
  const [viewMonth, setViewMonth] = useState(init.month);
  const coords = useDropdownPosition(open, wrapRef, dropdownRef, 320, 280);

  useEffect(() => {
    if (open) {
      const p = parseDate(value);
      setViewYear(p.year);
      setViewMonth(p.month);
    }
  }, [open, value]);

  useEffect(() => {
    if (!open) return;
    const handleOutside = (e) => {
      if (
        wrapRef.current &&
        !wrapRef.current.contains(e.target) &&
        dropdownRef.current &&
        !dropdownRef.current.contains(e.target)
      ) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handleOutside);
    return () => document.removeEventListener('mousedown', handleOutside);
  }, [open]);

  const emit = (val) => {
    if (disabled) return;
    if (typeof onChange === 'function') {
      onChange({ target: { value: val, name } });
    }
  };

  const selectDay = (dateStr) => {
    emit(dateStr);
    setOpen(false);
  };

  const selectToday = (e) => {
    e.preventDefault();
    e.stopPropagation();
    const now = new Date();
    const todayStr = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;
    emit(todayStr);
    setOpen(false);
  };

  const clearValue = (e) => {
    e.preventDefault();
    e.stopPropagation();
    emit('');
    setOpen(false);
  };

  const prevMonth = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (viewMonth === 0) {
      setViewYear((y) => y - 1);
      setViewMonth(11);
    } else {
      setViewMonth((m) => m - 1);
    }
  };

  const nextMonth = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (viewMonth === 11) {
      setViewYear((y) => y + 1);
      setViewMonth(0);
    } else {
      setViewMonth((m) => m + 1);
    }
  };

  let displayText = '';
  if (value && /^\d{4}-\d{2}-\d{2}/.test(value)) {
    const [y, m, d] = value.slice(0, 10).split('-');
    displayText = `${d}/${m}/${y}`;
  }

  const cells = getCalendarGrid(viewYear, viewMonth, value ? value.slice(0, 10) : '', min, max);
  const years = getYearOptions(viewYear);

  return (
    <div className={`month-picker-wrap custom-date-picker-wrap ${className}`} ref={wrapRef}>
      <input
        type="text"
        tabIndex={-1}
        aria-hidden="true"
        required={required}
        value={value || ''}
        onChange={() => {}}
        style={{
          position: 'absolute',
          opacity: 0,
          width: '1px',
          height: '1px',
          pointerEvents: 'none',
          clip: 'rect(0 0 0 0)',
          margin: '-1px',
        }}
      />
      <div
        role="button"
        aria-expanded={open}
        aria-haspopup="dialog"
        className={`month-picker-input ${open ? 'is-open' : ''} ${disabled ? 'is-disabled' : ''}`}
        onClick={() => !disabled && setOpen((o) => !o)}
        tabIndex={disabled ? -1 : 0}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            if (!disabled) setOpen((o) => !o);
          } else if (e.key === 'Escape') {
            setOpen(false);
          }
        }}
      >
        <span className={`month-picker-value ${!displayText ? 'is-placeholder' : ''}`}>
          {displayText || placeholder}
        </span>
        <div className="month-picker-icons">
          {allowClear && value && !disabled && (
            <span
              className="month-picker-clear"
              title="Xóa ngày"
              onClick={clearValue}
            >
              ✕
            </span>
          )}
          <Icon name="calendar" size={15} />
        </div>
      </div>

      {open &&
        createPortal(
          <div
            ref={dropdownRef}
            className="month-picker-dropdown date-picker-dropdown"
            style={{
              position: 'fixed',
              top: `${coords.top}px`,
              left: `${coords.left}px`,
              zIndex: 99999,
            }}
            onMouseDown={(e) => e.stopPropagation()}
            onClick={(e) => e.stopPropagation()}
          >
          <div className="month-picker-header">
            <button
              type="button"
              className="month-picker-nav-btn"
              title="Tháng trước"
              onClick={prevMonth}
            >
              ‹
            </button>

            <div className="date-picker-header-selectors">
              <select
                className="month-picker-year-select"
                value={viewMonth}
                onChange={(e) => {
                  e.stopPropagation();
                  setViewMonth(parseInt(e.target.value, 10));
                }}
                onClick={(e) => e.stopPropagation()}
              >
                {VI_MONTHS.map((name, idx) => (
                  <option key={idx} value={idx}>
                    {name}
                  </option>
                ))}
              </select>

              <select
                className="month-picker-year-select"
                value={viewYear}
                onChange={(e) => {
                  e.stopPropagation();
                  setViewYear(parseInt(e.target.value, 10));
                }}
                onClick={(e) => e.stopPropagation()}
              >
                {years.map((y) => (
                  <option key={y} value={y}>
                    Năm {y}
                  </option>
                ))}
              </select>
            </div>

            <button
              type="button"
              className="month-picker-nav-btn"
              title="Tháng sau"
              onClick={nextMonth}
            >
              ›
            </button>
          </div>

          <div className="date-picker-weekdays">
            {['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'].map((d) => (
              <span key={d} className="date-picker-weekday">
                {d}
              </span>
            ))}
          </div>

          <div className="date-picker-grid">
            {cells.map((cell, idx) => (
              <button
                key={idx}
                type="button"
                disabled={cell.disabled}
                className={`date-picker-cell ${cell.isSelected ? 'is-selected' : ''} ${
                  cell.isToday ? 'is-today' : ''
                } ${!cell.isCurrentMonth ? 'is-other-month' : ''} ${
                  cell.disabled ? 'is-disabled' : ''
                }`}
                onClick={(e) => {
                  e.preventDefault();
                  e.stopPropagation();
                  if (!cell.disabled) selectDay(cell.dateStr);
                }}
              >
                {cell.dayNumber}
              </button>
            ))}
          </div>

          <div className="month-picker-footer">
            <button
              type="button"
              className="month-picker-btn-link"
              onClick={selectToday}
            >
              Hôm nay
            </button>
            {allowClear && value && (
              <button
                type="button"
                className="month-picker-btn-link danger"
                onClick={clearValue}
              >
                Xóa
              </button>
            )}
          </div>
        </div>,
        document.body
      )}
    </div>
  );
}

/**
 * Bộ chọn Ngày & Giờ hoàn toàn bằng Tiếng Việt (thay thế input type="datetime-local")
 * Giá trị trả về: yyyy-MM-ddTHH:mm
 */
export function DateTimePicker({
  value = '',
  onChange,
  placeholder = 'Chọn ngày giờ...',
  disabled = false,
  required = false,
  allowClear = true,
  className = '',
  name,
  min,
  max,
}) {
  const [open, setOpen] = useState(false);
  const wrapRef = useRef(null);
  const dropdownRef = useRef(null);

  const init = parseDateTime(value);
  const [viewYear, setViewYear] = useState(init.year);
  const [viewMonth, setViewMonth] = useState(init.month);
  const [selectedDate, setSelectedDate] = useState(init.dateStr);
  const [selectedHour, setSelectedHour] = useState(init.hour);
  const [selectedMinute, setSelectedMinute] = useState(init.minute);
  const coords = useDropdownPosition(open, wrapRef, dropdownRef, 370, 290);

  useEffect(() => {
    if (open) {
      const p = parseDateTime(value);
      setViewYear(p.year);
      setViewMonth(p.month);
      setSelectedDate(p.dateStr);
      setSelectedHour(p.hour);
      setSelectedMinute(p.minute);
    }
  }, [open, value]);

  useEffect(() => {
    if (!open) return;
    const handleOutside = (e) => {
      if (
        wrapRef.current &&
        !wrapRef.current.contains(e.target) &&
        dropdownRef.current &&
        !dropdownRef.current.contains(e.target)
      ) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handleOutside);
    return () => document.removeEventListener('mousedown', handleOutside);
  }, [open]);

  const emit = (val) => {
    if (disabled) return;
    if (typeof onChange === 'function') {
      onChange({ target: { value: val, name } });
    }
  };

  const selectDay = (dateStr) => {
    setSelectedDate(dateStr);
    emit(`${dateStr}T${selectedHour}:${selectedMinute}`);
  };

  const updateHour = (h) => {
    setSelectedHour(h);
    emit(`${selectedDate}T${h}:${selectedMinute}`);
  };

  const updateMinute = (m) => {
    setSelectedMinute(m);
    emit(`${selectedDate}T${selectedHour}:${m}`);
  };

  const selectNow = (e) => {
    e.preventDefault();
    e.stopPropagation();
    const now = new Date();
    const y = now.getFullYear();
    const m = now.getMonth();
    const dStr = `${y}-${pad(m + 1)}-${pad(now.getDate())}`;
    const hStr = pad(now.getHours());
    const mStr = pad(now.getMinutes());
    setViewYear(y);
    setViewMonth(m);
    setSelectedDate(dStr);
    setSelectedHour(hStr);
    setSelectedMinute(mStr);
    emit(`${dStr}T${hStr}:${mStr}`);
    setOpen(false);
  };

  const clearValue = (e) => {
    e.preventDefault();
    e.stopPropagation();
    emit('');
    setOpen(false);
  };

  const prevMonth = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (viewMonth === 0) {
      setViewYear((y) => y - 1);
      setViewMonth(11);
    } else {
      setViewMonth((m) => m - 1);
    }
  };

  const nextMonth = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (viewMonth === 11) {
      setViewYear((y) => y + 1);
      setViewMonth(0);
    } else {
      setViewMonth((m) => m + 1);
    }
  };

  let displayText = '';
  if (value && /^\d{4}-\d{2}-\d{2}/.test(value)) {
    const dPart = value.slice(0, 10);
    const tPart = value.slice(11, 16);
    const [y, m, d] = dPart.split('-');
    displayText = `${d}/${m}/${y} ${tPart || '00:00'}`;
  }

  const cells = getCalendarGrid(
    viewYear,
    viewMonth,
    selectedDate,
    min ? min.slice(0, 10) : '',
    max ? max.slice(0, 10) : ''
  );
  const years = getYearOptions(viewYear);

  return (
    <div className={`month-picker-wrap custom-date-picker-wrap ${className}`} ref={wrapRef}>
      <input
        type="text"
        tabIndex={-1}
        aria-hidden="true"
        required={required}
        value={value || ''}
        onChange={() => {}}
        style={{
          position: 'absolute',
          opacity: 0,
          width: '1px',
          height: '1px',
          pointerEvents: 'none',
          clip: 'rect(0 0 0 0)',
          margin: '-1px',
        }}
      />
      <div
        role="button"
        aria-expanded={open}
        aria-haspopup="dialog"
        className={`month-picker-input ${open ? 'is-open' : ''} ${disabled ? 'is-disabled' : ''}`}
        onClick={() => !disabled && setOpen((o) => !o)}
        tabIndex={disabled ? -1 : 0}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            if (!disabled) setOpen((o) => !o);
          } else if (e.key === 'Escape') {
            setOpen(false);
          }
        }}
      >
        <span className={`month-picker-value ${!displayText ? 'is-placeholder' : ''}`}>
          {displayText || placeholder}
        </span>
        <div className="month-picker-icons">
          {allowClear && value && !disabled && (
            <span
              className="month-picker-clear"
              title="Xóa thời gian"
              onClick={clearValue}
            >
              ✕
            </span>
          )}
          <Icon name="calendar" size={15} />
        </div>
      </div>

      {open &&
        createPortal(
          <div
            ref={dropdownRef}
            className="month-picker-dropdown date-picker-dropdown datetime-picker-dropdown"
            style={{
              position: 'fixed',
              top: `${coords.top}px`,
              left: `${coords.left}px`,
              zIndex: 99999,
            }}
            onMouseDown={(e) => e.stopPropagation()}
            onClick={(e) => e.stopPropagation()}
          >
          <div className="month-picker-header">
            <button
              type="button"
              className="month-picker-nav-btn"
              title="Tháng trước"
              onClick={prevMonth}
            >
              ‹
            </button>

            <div className="date-picker-header-selectors">
              <select
                className="month-picker-year-select"
                value={viewMonth}
                onChange={(e) => {
                  e.stopPropagation();
                  setViewMonth(parseInt(e.target.value, 10));
                }}
                onClick={(e) => e.stopPropagation()}
              >
                {VI_MONTHS.map((name, idx) => (
                  <option key={idx} value={idx}>
                    {name}
                  </option>
                ))}
              </select>

              <select
                className="month-picker-year-select"
                value={viewYear}
                onChange={(e) => {
                  e.stopPropagation();
                  setViewYear(parseInt(e.target.value, 10));
                }}
                onClick={(e) => e.stopPropagation()}
              >
                {years.map((y) => (
                  <option key={y} value={y}>
                    Năm {y}
                  </option>
                ))}
              </select>
            </div>

            <button
              type="button"
              className="month-picker-nav-btn"
              title="Tháng sau"
              onClick={nextMonth}
            >
              ›
            </button>
          </div>

          <div className="date-picker-weekdays">
            {['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'].map((d) => (
              <span key={d} className="date-picker-weekday">
                {d}
              </span>
            ))}
          </div>

          <div className="date-picker-grid">
            {cells.map((cell, idx) => (
              <button
                key={idx}
                type="button"
                disabled={cell.disabled}
                className={`date-picker-cell ${cell.isSelected ? 'is-selected' : ''} ${
                  cell.isToday ? 'is-today' : ''
                } ${!cell.isCurrentMonth ? 'is-other-month' : ''} ${
                  cell.disabled ? 'is-disabled' : ''
                }`}
                onClick={(e) => {
                  e.preventDefault();
                  e.stopPropagation();
                  if (!cell.disabled) selectDay(cell.dateStr);
                }}
              >
                {cell.dayNumber}
              </button>
            ))}
          </div>

          <div className="picker-time-bar">
            <span className="picker-time-label">Giờ:</span>
            <div className="picker-time-controls">
              <select
                className="picker-time-select"
                value={selectedHour}
                onChange={(e) => updateHour(e.target.value)}
                onClick={(e) => e.stopPropagation()}
              >
                {Array.from({ length: 24 }, (_, i) => pad(i)).map((h) => (
                  <option key={h} value={h}>
                    {h} giờ
                  </option>
                ))}
              </select>
              <span className="picker-time-sep">:</span>
              <select
                className="picker-time-select"
                value={selectedMinute}
                onChange={(e) => updateMinute(e.target.value)}
                onClick={(e) => e.stopPropagation()}
              >
                {Array.from({ length: 60 }, (_, i) => pad(i)).map((m) => (
                  <option key={m} value={m}>
                    {m} phút
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="month-picker-footer">
            <button
              type="button"
              className="month-picker-btn-link"
              onClick={selectNow}
            >
              Hiện tại
            </button>
            <div style={{ display: 'flex', gap: '8px' }}>
              {allowClear && value && (
                <button
                  type="button"
                  className="month-picker-btn-link danger"
                  onClick={clearValue}
                >
                  Xóa
                </button>
              )}
              <button
                type="button"
                className="btn btn-primary btn-sm"
                style={{ padding: '3px 12px', fontSize: '12px', height: '26px' }}
                onClick={() => setOpen(false)}
              >
                Xong
              </button>
            </div>
          </div>
        </div>,
        document.body
      )}
    </div>
  );
}

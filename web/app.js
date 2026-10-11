/* Lịch Âm Dương – bản web (PWA) cho iPhone/iPad. Nhà phát triển: Công ty cổ phần Công nghệ ITD Việt Nam. */
(function () {
  'use strict';
  var L = window.Lunar;
  var app = document.getElementById('app');

  // ---------- Tiện ích ----------
  function esc(s) {
    return String(s).replace(/[&<>"']/g, function (c) {
      return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
    });
  }
  function svg(p) { return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">' + p + '</svg>'; }
  var I = {
    cal: svg('<rect x="3" y="5" width="18" height="16" rx="2"/><path d="M3 10h18M8 3v4M16 3v4"/>'),
    day: svg('<rect x="5" y="3" width="14" height="18" rx="2"/><path d="M9 8h6M9 12h6"/>'),
    conv: svg('<path d="M7 7h11l-3-3M17 17H6l3 3"/>'),
    bell: svg('<path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10 21h4"/>'),
    set: svg('<circle cx="12" cy="12" r="3"/><path d="M12 2v3M12 19v3M4.2 4.2l2.1 2.1M17.7 17.7l2.1 2.1M2 12h3M19 12h3M4.2 19.8l2.1-2.1M17.7 6.3l2.1-2.1"/>'),
    left: svg('<path d="M15 18l-6-6 6-6"/>'),
    right: svg('<path d="M9 18l6-6-6-6"/>'),
    up: svg('<path d="M6 15l6-6 6 6"/>'),
    down: svg('<path d="M6 9l6 6 6-6"/>'),
    plus: svg('<path d="M12 5v14M5 12h14"/>'),
    close: svg('<path d="M6 6l12 12M18 6L6 18"/>'),
    trash: svg('<path d="M4 7h16M10 11v6M14 11v6M6 7l1 13h10l1-13M9 7V4h6v3"/>'),
    moon: svg('<path d="M20 14.5A8 8 0 1 1 9.5 4a6.5 6.5 0 0 0 10.5 10.5z"/>'),
    sun: svg('<circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/>'),
    phone: svg('<path d="M22 16.9v3a2 2 0 0 1-2.2 2 19.8 19.8 0 0 1-8.6-3.1 19.5 19.5 0 0 1-6-6A19.8 19.8 0 0 1 2.1 4.2 2 2 0 0 1 4.1 2h3a2 2 0 0 1 2 1.7c.1.9.4 1.8.7 2.7a2 2 0 0 1-.5 2.1L8 9.8a16 16 0 0 0 6 6l1.3-1.3a2 2 0 0 1 2.1-.4c.9.3 1.8.6 2.7.7a2 2 0 0 1 1.7 2z"/>'),
    mail: svg('<rect x="2" y="4" width="20" height="16" rx="2"/><path d="M22 6l-10 7L2 6"/>'),
    web: svg('<circle cx="12" cy="12" r="10"/><path d="M2 12h20M12 2a15 15 0 0 1 0 20M12 2a15 15 0 0 0 0 20"/>'),
    pin: svg('<path d="M21 10c0 7-9 13-9 13S3 17 3 10a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>'),
    info: svg('<circle cx="12" cy="12" r="9"/><path d="M12 8h.01M11 12h1v5h1"/>'),
    swap: svg('<path d="M7 4v16M3 16l4 4 4-4M17 20V4M13 8l4-4 4 4"/>'),
    calAdd: svg('<rect x="3" y="5" width="18" height="16" rx="2"/><path d="M3 10h18M8 3v4M16 3v4M12 13v5M9.5 15.5h5"/>')
  };
  var store = {
    get: function (k, d) { try { var v = localStorage.getItem('lad.' + k); return v == null ? d : JSON.parse(v); } catch (e) { return d; } },
    set: function (k, v) { try { localStorage.setItem('lad.' + k, JSON.stringify(v)); } catch (e) { /* bộ nhớ bị chặn: vẫn chạy bình thường */ } }
  };
  function todayJd() { var n = new Date(); return L.jdFromDate(n.getDate(), n.getMonth() + 1, n.getFullYear()); }
  function wrap(v, a, b) { return v < a ? b : (v > b ? a : v); }
  function clamp(v, a, b) { return Math.max(a, Math.min(b, v)); }
  function pad(n) { return (n < 10 ? '0' : '') + n; }
  function solarW(jd) { var s = L.jdToDate(jd); return L.WD_SHORT[jd % 7] + ', ' + s.d + '/' + s.m + '/' + s.y; }
  function lunarOf(jd) { return L.lunarShort(L.solarToLunar(jd)) + ' âm'; }
  function countdown(jd) { var n = jd - todayJd(); return n === 0 ? 'hôm nay' : (n === 1 ? 'ngày mai' : 'còn ' + n + ' ngày'); }

  // ---------- Trạng thái ----------
  var t0 = todayJd(), tl = L.solarToLunar(t0), ts = L.jdToDate(t0);
  var S = {
    dest: 'month', sel: t0, vm: { y: ts.y, m: ts.m }, editing: null, filter: 'all',
    theme: store.get('theme', 'system'), size: store.get('size', 'lg'),
    conv: { mode: 's2l', sd: ts.d, sm: ts.m, sy: ts.y, ld: tl.d, lm: tl.m, ly: tl.y, leap: tl.leap }
  };
  var reminders = store.get('reminders', null);
  if (!reminders) {
    reminders = [{ id: 1, title: 'Mùng 1 & Rằm', cal: 'lunar', rep: 'month', d: 1, m: 1, y: ts.y, extra: [15], before: 0, hour: 6, minute: 0, on: true }];
    store.set('reminders', reminders);
  }
  // Dữ liệu cũ chưa có giờ chuông riêng: lấy theo giờ thông báo
  reminders.forEach(function (r) {
    if (r.aHour == null) { r.aBefore = r.before; r.aHour = r.hour; r.aMinute = r.minute; }
    if (!r.snooze) r.snooze = 10;
  });
  function saveReminders() { store.set('reminders', reminders); }
  var SIZES = { md: 1.0, lg: 1.1, xl: 1.25 };
  function applyPrefs() {
    document.documentElement.setAttribute('data-theme', S.theme);
    document.documentElement.style.setProperty('--scale', SIZES[S.size] || 1.1);
  }

  function ruleText(r) {
    var cal = r.cal === 'lunar' ? 'âm lịch' : 'dương lịch';
    if (r.rep === 'month') {
      var days = [r.d].concat(r.extra || []).sort(function (a, b) { return a - b; })
        .map(function (d) { return r.cal === 'lunar' ? L.dayName(d).toLowerCase() : 'ngày ' + d; }).join(' và ');
      return days.charAt(0).toUpperCase() + days.slice(1) + ' ' + cal + ' · Hằng tháng';
    }
    if (r.rep === 'year') return r.cal === 'lunar' ? 'Ngày ' + r.d + ' tháng ' + L.MONTH[r.m] + ' ' + cal + ' · Hằng năm' : 'Ngày ' + r.d + '/' + r.m + ' ' + cal + ' · Hằng năm';
    return r.cal === 'lunar' ? 'Ngày ' + r.d + ' tháng ' + L.MONTH[r.m] + ' năm ' + r.y + ' ' + cal : 'Ngày ' + r.d + '/' + r.m + '/' + r.y;
  }
  function atText(b, h, m) { return pad(h) + ':' + pad(m) + (b ? ', trước ' + b + ' ngày' : ' đúng ngày'); }
  function ringSame(r) { return !!r.alarm && (r.aBefore | 0) === (r.before | 0) && r.aHour === r.hour && r.aMinute === r.minute; }
  function alertText(r) {
    if (ringSame(r)) return 'Đổ chuông ' + atText(r.aBefore, r.aHour, r.aMinute);
    return 'Thông báo ' + atText(r.before, r.hour, r.minute) + (r.alarm ? ' · Chuông ' + atText(r.aBefore, r.aHour, r.aMinute) : '');
  }
  /* Ô chọn mốc báo tự do: số ngày trước (0–30) + giờ:phút bất kỳ (ô chọn giờ của máy) */
  function dayTime(prefix, b, h, m, label) {
    return '<div class="dt"><div class="dt-days"><span class="lab">Ngày báo</span><div class="row" style="gap:6px">' +
      '<button class="sq" data-act="ed-days" data-arg="' + prefix + ':-1" aria-label="Bớt một ngày">−</button>' +
      '<b class="dt-v">' + (b ? 'Trước<br>' + b + ' ngày' : 'Đúng<br>ngày') + '</b>' +
      '<button class="sq" data-act="ed-days" data-arg="' + prefix + ':1" aria-label="Thêm một ngày">+</button></div></div>' +
      '<label class="dt-time"><span class="lab">' + esc(label) + '</span>' +
      '<input type="time" class="time-in" data-time="' + prefix + '" value="' + pad(h) + ':' + pad(m) + '" aria-label="' + esc(label) + '"></label></div>';
  }
  function maxDay(r) {
    if (r.cal === 'lunar') return 30;
    if (r.rep === 'month') return 31;
    return L.dim(r.m, r.rep === 'once' ? r.y : 2028);
  }

  // ---------- Thành phần giao diện ----------
  function bar(title, sub, right, back, small) {
    return '<div class="bar">' + (back ? '<button class="icon-btn" data-act="' + back + '" aria-label="Quay lại">' + I.left + '</button>' : '') +
      '<div class="col" style="flex:1"><h1' + (small ? ' class="sm"' : '') + '>' + esc(title) + '</h1>' + (sub ? '<div class="sub">' + esc(sub) + '</div>' : '') + '</div>' + (right || '') + '</div>';
  }
  function gearBtn() { return S.wide ? '' : '<button class="icon-btn" data-act="go" data-arg="settings" aria-label="Cài đặt và giới thiệu">' + I.set + '</button>'; }
  function seg(items, current, act) {
    return '<div class="seg" role="group">' + items.map(function (it) {
      return '<button data-act="' + act + '" data-arg="' + it[0] + '" aria-pressed="' + (String(it[0]) === String(current)) + '">' + esc(it[1]) + '</button>';
    }).join('') + '</div>';
  }
  function chips(items, current, act) {
    return '<div class="chips">' + items.map(function (it) {
      return '<button class="chipbtn" data-act="' + act + '" data-arg="' + it[0] + '" aria-pressed="' + (String(it[0]) === String(current)) + '">' + esc(it[1]) + '</button>';
    }).join('') + '</div>';
  }
  function stepper(label, value, act, field) {
    return '<div class="stepper"><span class="lab">' + esc(label) + '</span>' +
      '<button data-act="' + act + '" data-arg="' + field + ':1" aria-label="Tăng ' + esc(label.toLowerCase()) + '">' + I.up + '</button>' +
      '<span class="val">' + esc(value) + '</span>' +
      '<button data-act="' + act + '" data-arg="' + field + ':-1" aria-label="Giảm ' + esc(label.toLowerCase()) + '">' + I.down + '</button></div>';
  }

  function lunarSubtitle(y, m) {
    var a = L.solarToLunar(L.jdFromDate(1, m, y)), b = L.solarToLunar(L.jdFromDate(L.dim(m, y), m, y));
    function ml(x) { return x.m + (x.leap ? ' nhuận' : ''); }
    var months = (a.m === b.m && a.leap === b.leap) ? 'Tháng ' + ml(a) + ' âm lịch' : 'Tháng ' + ml(a) + ' – ' + ml(b) + ' âm lịch';
    var years = a.y === b.y ? 'Năm ' + L.yearCC(a.y) : 'Năm ' + L.yearCC(a.y) + ' – ' + L.yearCC(b.y);
    return months + ' · ' + years;
  }

  function monthGrid() {
    var y = S.vm.y, m = S.vm.m, first = L.jdFromDate(1, m, y), off = first % 7, start = first - off;
    var total = Math.ceil((off + L.dim(m, y)) / 7) * 7, today = todayJd(), h = '';
    for (var i = 0; i < total; i++) {
      var jd = start + i, info = L.dayInfo(jd), cls = ['cell'];
      if (info.s.m !== m) cls.push('out');
      if (info.wd === 6) cls.push('sun');
      if (info.special) cls.push('sp');
      if (jd === today) cls.push('today');
      else if (jd === S.sel) cls.push('sel');
      var aria = info.weekday + ' ' + info.solarText + ', ' + L.lunarText(info.l) + ' âm lịch' + (info.events.length ? ', ' + info.events.join(', ') : '');
      h += '<button class="' + cls.join(' ') + '" data-act="sel" data-arg="' + jd + '" aria-label="' + esc(aria) + '">' +
        '<span class="n">' + info.s.d + '</span><span class="l">' + (info.l.d === 1 ? '1/' + info.l.m : info.l.d) + '</span>' +
        (info.events.length ? '<span class="dot"></span>' : '') + '</button>';
    }
    return '<div class="wk"><div>T2</div><div>T3</div><div>T4</div><div>T5</div><div>T6</div><div>T7</div><div class="sun">CN</div></div>' +
      '<div class="grid" id="month-grid">' + h + '</div>';
  }

  function viewMonth(wide) {
    var info = L.dayInfo(S.sel), h = bar('Tháng ' + S.vm.m + ', ' + S.vm.y, lunarSubtitle(S.vm.y, S.vm.m),
      '<button class="icon-btn" data-act="m-prev" aria-label="Tháng trước">' + I.left + '</button>' +
      '<button class="icon-btn" data-act="m-next" aria-label="Tháng sau">' + I.right + '</button>' +
      '<button class="pill-btn" data-act="today">Hôm nay</button>' + gearBtn(), '', true);
    h += '<div class="wrap stack">' + monthGrid();
    if (!wide) {
      h += '<section class="card"><div class="row" style="align-items:flex-start"><div class="badge"><b>' + info.l.d + '</b><span>tháng ' + info.l.m + '</span></div>' +
        '<div class="col"><div class="t1">' + info.weekday + ', ' + info.solarText + '</div><div class="t2">' + esc(L.lunarText(info.l)) + ' âm lịch</div>' +
        '<div class="mut">Ngày ' + info.dayCC + ' · Tháng ' + info.monthCC + ' · Năm ' + info.yearCC + '</div></div></div>' +
        info.events.map(function (e) { return '<div class="ev">' + esc(e) + '</div>'; }).join('') +
        '<div class="label">Giờ hoàng đạo</div><div class="chips">' + info.gio.map(function (g) { return '<span class="chip"><b>' + g.chi + '</b> ' + g.range + '</span>'; }).join('') + '</div>' +
        '<button class="link" data-act="go" data-arg="day">Xem chi tiết ngày →</button></section>';
    } else {
      var y = S.vm.y, m = S.vm.m, list = '';
      for (var d = 1; d <= L.dim(m, y); d++) {
        var jd = L.jdFromDate(d, m, y), x = L.dayInfo(jd);
        if (x.events.length) {
          list += '<button class="rem' + (jd === S.sel ? ' sel' : '') + '" data-act="sel" data-arg="' + jd + '" style="padding:8px 10px">' +
            '<div class="col" style="width:48px;align-items:center"><b style="font-size:1.1rem">' + d + '</b><span class="mut" style="font-size:0.7rem">' + L.lunarShort(x.l) + ' âm</span></div>' +
            '<span class="t2">' + esc(x.events.join(' · ')) + '</span></button>';
        }
      }
      h += '<div class="stack-s"><h2 class="t1" style="margin:4px 0 0 4px">Sự kiện trong tháng</h2>' + (list || '<div class="mut">Không có sự kiện</div>') + '</div>';
    }
    return h + '</div>';
  }

  function viewDay() {
    var x = L.dayInfo(S.sel);
    return bar(S.wide ? 'Chi tiết ngày' : 'Tháng ' + x.s.m + ', ' + x.s.y, '', '<button class="pill-btn" data-act="today">Hôm nay</button>') +
      '<div class="wrap stack">' +
      '<div class="big-day"><button class="round" data-act="d-step" data-arg="-1" aria-label="Ngày trước">' + I.left + '</button>' +
      '<div class="mid"><div class="t1 mut" style="font-size:1.05rem">' + x.weekday + '</div><div class="big-num">' + x.s.d + '</div><div class="mut">Dương lịch ' + x.solarText + '</div></div>' +
      '<button class="round" data-act="d-step" data-arg="1" aria-label="Ngày sau">' + I.right + '</button></div>' +
      '<section class="card"><div class="row" style="align-items:baseline;gap:8px"><b style="font-family:var(--num);font-size:2.6rem;line-height:1">' + x.l.d + '</b>' +
      '<span class="t1">tháng ' + L.MONTH[x.l.m] + (x.l.leap ? ' nhuận' : '') + ' âm lịch</span></div>' +
      '<div class="g3"><div class="box"><span>Ngày</span><b>' + x.dayCC + '</b></div><div class="box"><span>Tháng</span><b>' + x.monthCC + '</b></div><div class="box"><span>Năm</span><b>' + x.yearCC + '</b></div></div>' +
      '<div class="chips"><span class="chip">Tiết khí: <b>' + x.tiet + '</b></span><span class="chip">' + x.moon + '</span><span class="chip">' + (x.len === 30 ? 'Tháng đủ (30 ngày)' : 'Tháng thiếu (29 ngày)') + '</span></div></section>' +
      x.events.map(function (e) { return '<div class="ev-wide">' + esc(e) + '</div>'; }).join('') +
      '<h2 class="t1" style="margin:0">Giờ hoàng đạo</h2><div class="g3">' +
      x.gio.map(function (g) { return '<div class="gio"><b>' + g.chi + '</b><span>' + g.range + '</span></div>'; }).join('') + '</div>' +
      '<button class="primary" data-act="d-remind">' + I.plus + 'Thêm nhắc lịch cho ngày này</button></div>';
  }

  function convResult() {
    var c = S.conv;
    if (c.mode === 's2l') {
      var sd = Math.min(c.sd, L.dim(c.sm, c.sy)), jd = L.jdFromDate(sd, c.sm, c.sy), x = L.dayInfo(jd);
      return { ok: true, jd: jd, l: x.l, main: L.lunarText(x.l), sub: 'Năm ' + x.yearCC + ' (' + x.l.y + ') · ' + L.lunarShort(x.l) + ' âm lịch',
        cc: 'Ngày ' + x.dayCC + ' · Tháng ' + x.monthCC + ' · Năm ' + x.yearCC,
        tags: [x.weekday + ', ' + x.solarText, x.len === 30 ? 'Tháng đủ (30 ngày)' : 'Tháng thiếu (29 ngày)'] };
    }
    var useLeap = c.leap && L.leapMonthOf(c.ly) === c.lm, j = L.lunarToSolar(c.ld, c.lm, c.ly, useLeap);
    if (!j) return { ok: false, err: 'Tháng ' + L.MONTH[c.lm] + (useLeap ? ' nhuận' : '') + ' năm ' + c.ly + ' là tháng thiếu, chỉ có 29 ngày. Hãy chọn ngày 29 trở xuống.' };
    var y = L.dayInfo(j);
    return { ok: true, jd: j, l: y.l, main: y.weekday + ', ' + y.solarText, sub: L.lunarText(y.l) + ' năm ' + y.yearCC,
      cc: 'Ngày ' + y.dayCC + ' · Tháng ' + y.monthCC + ' · Năm ' + y.yearCC, tags: [y.len === 30 ? 'Tháng đủ (30 ngày)' : 'Tháng thiếu (29 ngày)'] };
  }

  function viewConvert() {
    var c = S.conv, r = convResult(), s2l = c.mode === 's2l', lp = L.leapMonthOf(c.ly), h = bar('Đổi ngày', '', gearBtn());
    h += '<div class="wrap stack">' + seg([['s2l', 'Dương → Âm'], ['l2s', 'Âm → Dương']], c.mode, 'cv-mode') +
      '<div class="label">' + (s2l ? 'Nhập ngày dương lịch' : 'Nhập ngày âm lịch') + '</div><div class="steppers">';
    if (s2l) h += stepper('Ngày', Math.min(c.sd, L.dim(c.sm, c.sy)), 'cv-step', 'sd') + stepper('Tháng', c.sm, 'cv-step', 'sm') + stepper('Năm', c.sy, 'cv-step', 'sy');
    else h += stepper('Ngày âm', c.ld, 'cv-step', 'ld') + stepper('Tháng âm', c.lm, 'cv-step', 'lm') + stepper('Năm', c.ly, 'cv-step', 'ly');
    h += '</div>';
    if (!s2l) {
      h += '<div class="row"><span class="mut" style="flex:1">' + (lp ? 'Năm ' + c.ly + ' có tháng ' + lp + ' nhuận' : 'Năm ' + c.ly + ' không có tháng nhuận') + '</span>' +
        '<button class="chipbtn" data-act="cv-leap" aria-pressed="' + (c.leap && lp === c.lm) + '"' + (lp === c.lm ? '' : ' disabled') + '>Tháng nhuận</button></div>';
    }
    h += '<div class="row" style="justify-content:center"><button class="round" data-act="cv-swap" aria-label="Đảo chiều, dùng kết quả làm ngày nhập">' + I.swap + '</button></div>' +
      '<section class="card" aria-live="polite"><div class="label">' + (s2l ? 'Kết quả âm lịch' : 'Kết quả dương lịch') + '</div>' +
      (r.ok ? '<div style="font-family:var(--num);font-size:1.75rem;font-weight:700;color:var(--acc);line-height:1.2">' + esc(r.main) + '</div>' +
        '<div class="t1" style="font-size:0.95rem">' + esc(r.sub) + '</div><div class="mut">' + esc(r.cc) + '</div>' +
        '<div class="chips">' + r.tags.map(function (t) { return '<span class="chip">' + esc(t) + '</span>'; }).join('') + '</div>'
        : '<div style="color:var(--acc);font-weight:600">' + esc(r.err) + '</div>') + '</section>' +
      '<button class="primary" data-act="cv-remind"' + (r.ok ? '' : ' disabled') + '>' + I.bell + 'Đặt nhắc lịch cho ngày này</button></div>';
    return h;
  }

  function viewReminders(selectedId) {
    var now = todayJd();
    var rows = reminders.map(function (r) { return { r: r, next: L.occurrences(r, now, 1)[0] }; })
      .sort(function (a, b) { return (a.next == null) - (b.next == null) || (a.next || 0) - (b.next || 0); });
    var first = rows.filter(function (x) { return x.r.on && x.next != null; })[0];
    var vis = rows.filter(function (x) { return S.filter === 'all' || x.r.cal === S.filter; });
    var h = bar('Nhắc lịch', '', gearBtn()) + '<div class="wrap stack">';
    if (first) {
      h += '<section class="hero"><div class="cnt">' + (first.next === now ? '<b style="font-size:0.95rem;font-family:var(--font)">Hôm nay</b>' : '<b>' + (first.next - now) + '</b><span>ngày</span>') + '</div><div class="col"><span style="font-size:0.75rem;font-weight:600">Sắp tới gần nhất</span>' +
        '<span class="t1">' + esc(first.r.title) + '</span><span style="font-size:0.82rem">' + solarW(first.next) + ' · ' + lunarOf(first.next) + '</span></div></section>';
    }
    h += seg([['all', 'Tất cả'], ['lunar', 'Âm lịch'], ['solar', 'Dương lịch']], S.filter, 'filter');
    if (!vis.length) h += '<div class="empty">Chưa có nhắc lịch. Bấm “Tạo nhắc” để thêm ngày giỗ, rằm, mùng 1 hoặc ngày kỷ niệm.</div>';
    vis.forEach(function (x) {
      var r = x.r;
      h += '<div class="rem' + (r.on ? '' : ' off') + (r.id === selectedId ? ' sel' : '') + '">' +
        '<button class="row" style="flex:1;min-width:0;text-align:left" data-act="rem-open" data-arg="' + r.id + '">' +
        '<span class="ic ' + r.cal + '">' + (r.cal === 'lunar' ? I.moon : I.sun) + '</span>' +
        '<span class="col"><span class="t1">' + esc(r.title) + '</span><span class="mut">' + esc(ruleText(r)) + '</span>' +
        (x.next != null ? '<span class="t2" style="font-weight:600">' + solarW(x.next) + ' · ' + countdown(x.next) + '</span><span class="mut">' + lunarOf(x.next) + ' · ' + alertText(r) + '</span>' : '<span class="mut">Đã qua</span>') +
        '</span></button>' +
        '<button class="switch" role="switch" aria-checked="' + r.on + '" aria-label="Bật hoặc tắt nhắc ' + esc(r.title) + '" data-act="rem-toggle" data-arg="' + r.id + '"><span></span></button></div>';
    });
    if (reminders.length) {
      h += '<button class="secondary" data-act="ics-all">' + I.calAdd + '&nbsp;Thêm tất cả vào ứng dụng Lịch</button>' +
        '<div class="note">' + I.info + '<div>Trên iPhone, trình duyệt không tự báo được khi ứng dụng đang đóng. Bấm <b>“Thêm vào ứng dụng Lịch”</b> để chuyển các lần nhắc sang ứng dụng Lịch của máy; Lịch sẽ báo đúng giờ, kể cả khi tắt màn hình.</div></div>';
    }
    h += '<button class="fab" data-act="new">' + I.plus + 'Tạo nhắc</button></div>';
    return h;
  }

  function viewEditor() {
    var r = S.editing, lunar = r.cal === 'lunar', md = maxDay(r), d = Math.min(r.d, md), now = todayJd();
    var rr = Object.assign({}, r, { d: d }), occ = L.occurrences(rr, now, 4);
    var h = '<div class="bar"><button class="icon-btn" data-act="ed-close" aria-label="Đóng">' + I.close + '</button>' +
      '<h1 style="font-size:1.25rem">' + (r.id ? 'Sửa nhắc lịch' : 'Nhắc lịch mới') + '</h1>' +
      (r.id ? '<button class="icon-btn" data-act="ed-del" aria-label="Xoá nhắc lịch">' + I.trash + '</button>' : '') +
      '<button class="pill-btn" data-act="ed-save" style="border-color:var(--acc);color:var(--acc)">Lưu</button></div>';
    h += '<div class="wrap stack">' +
      '<div class="stack-s"><label class="label" for="ed-title">Tên nhắc lịch</label><input id="ed-title" class="field" type="text" value="' + esc(r.title) + '" placeholder="Ví dụ: Giỗ ông nội" autocomplete="off"></div>' +
      '<div class="stack-s"><div class="label">Theo lịch</div>' + seg([['lunar', 'Âm lịch'], ['solar', 'Dương lịch']], r.cal, 'ed-cal') + '</div>' +
      '<div class="stack-s"><div class="label">Lặp lại</div>' + seg([['once', 'Một lần'], ['month', 'Hằng tháng'], ['year', 'Hằng năm']], r.rep, 'ed-rep') + '</div>' +
      '<div class="stack-s"><div class="label">' + (r.rep === 'month' ? 'Ngày trong tháng' : 'Ngày tháng') + '</div><div class="steppers">' +
      stepper(lunar ? 'Ngày âm' : 'Ngày', d, 'ed-step', 'd') +
      (r.rep !== 'month' ? stepper(lunar ? 'Tháng âm' : 'Tháng', r.m, 'ed-step', 'm') : '') +
      (r.rep === 'once' ? stepper('Năm', r.y, 'ed-step', 'y') : '') + '</div>';
    if (r.rep === 'month') {
      var second = (r.extra || [])[0];
      h += '<div class="row"><span class="t2" style="flex:1">' + (second == null ? 'Thêm một ngày nữa trong tháng' : 'Ngày thứ hai: ' + (lunar ? L.dayName(second) : 'ngày ' + second)) + '</span>' +
        '<button class="switch" role="switch" aria-checked="' + (second != null) + '" aria-label="Thêm ngày thứ hai" data-act="ed-extra"><span></span></button></div>' +
        (second != null ? '<div class="steppers" style="max-width:50%">' + stepper('Ngày thứ hai', second, 'ed-step', 'x') + '</div>' : '');
    }
    h += '<div style="color:var(--acc);font-weight:600">' + esc(ruleText(rr)) + '</div>';
    if (lunar) h += '<div class="note">' + I.info + '<div>Tháng âm thiếu (29 ngày) sẽ nhắc vào ngày 29 nếu bạn chọn ngày 30.' + (r.rep === 'year' ? '<br>Năm có tháng nhuận trùng tháng đã chọn: nhắc ở tháng chính, không nhắc lại ở tháng nhuận.' : '') + '</div></div>';
    h += '</div><div class="stack-s"><div class="label">Thông báo</div>' + dayTime('n', r.before, r.hour, r.minute, 'Giờ thông báo') +
      '<div class="mut">' + (ringSame(r) ? 'Trùng giờ chuông: chỉ đổ chuông, không hiện thêm thông báo.' : 'Chạm vào giờ để chọn giờ, phút bất kỳ.') + '</div></div>' +
      '<section class="card"><div class="row"><span class="t1" style="flex:1">Đổ chuông như báo thức</span>' +
      '<button class="switch" role="switch" aria-checked="' + !!r.alarm + '" aria-label="Đổ chuông như báo thức" data-act="ed-alarm"><span></span></button></div>' +
      (r.alarm
        ? '<div class="label">Giờ đổ chuông (chọn tự do)</div>' + dayTime('a', r.aBefore, r.aHour, r.aMinute, 'Giờ đổ chuông') +
          '<div style="color:var(--acc);font-weight:600">Đổ chuông lúc ' + esc(atText(r.aBefore, r.aHour, r.aMinute)) + '</div>' +
          '<div class="label">Báo lại sau</div>' + chips([[5, '5 phút'], [10, '10 phút'], [15, '15 phút'], [30, '30 phút']], r.snooze || 10, 'ed-snooze') +
          '<div class="mut" style="line-height:1.5">Trên <b>Android</b>: điện thoại đổ chuông và rung liên tục, hiện màn hình chuông kể cả khi khoá máy, đến khi bấm Tắt hoặc Báo lại.<br>Trên <b>iPhone</b>: Apple không cho ứng dụng web đổ chuông như Đồng hồ; khi thêm vào ứng dụng Lịch, giờ chuông thành một lần báo riêng. Tuỳ chọn được giữ khi chuyển sang máy Android.</div>'
        : '<div class="mut" style="line-height:1.5">Đang tắt: chỉ hiện thông báo. Bật để điện thoại đổ chuông và rung liên tục vào giờ bạn chọn.</div>') + '</section>' +
      '<section class="card" aria-live="polite"><h2 class="t1" style="margin:0;font-size:0.95rem">Các lần nhắc sắp tới</h2>' +
      (occ.length ? occ.map(function (j) {
        var s = L.jdToDate(j);
        function at(b, h, m) { var a = L.jdToDate(j - (b | 0)); return pad(h) + ':' + pad(m) + ' ngày ' + a.d + '/' + a.m + '/' + a.y; }
        return '<div class="occ"><div class="dn"><b>' + s.d + '</b><span>th ' + s.m + '</span></div><div class="col"><span class="t2" style="font-weight:600">' + solarW(j) + ' · ' + lunarOf(j) + '</span>' +
          (ringSame(r) ? '' : '<span class="mut">Thông báo ' + at(r.before, r.hour, r.minute) + ' · ' + countdown(j) + '</span>') +
          (r.alarm ? '<span class="mut" style="color:var(--acc);font-weight:600">Đổ chuông ' + at(r.aBefore, r.aHour, r.aMinute) + '</span>' : '') + '</div></div>';
      }).join('') : '<div class="mut">Ngày đã chọn đã qua. Hãy chọn năm khác hoặc bật lặp lại.</div>') + '</section>' +
      '<button class="primary" data-act="ed-save">Lưu nhắc lịch</button></div>';
    return h;
  }

  function isIos() { return /iphone|ipad|ipod/i.test(navigator.userAgent) || (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1); }
  function isStandalone() { return window.navigator.standalone === true || window.matchMedia('(display-mode: standalone)').matches; }

  function viewSettings() {
    var h = bar('Cài đặt', '', '', S.wide ? '' : 'go-month') + '<div class="wrap stack">' +
      '<div class="stack-s"><div class="label">Giao diện</div>' + seg([['light', 'Sáng'], ['dark', 'Tối'], ['system', 'Theo máy']], S.theme, 'theme') +
      '<div class="mut">Giao diện tối dùng nền đen tuyền, giúp tiết kiệm pin trên màn hình OLED.</div></div>' +
      '<div class="stack-s"><div class="label">Cỡ chữ</div>' + chips([['md', 'Vừa'], ['lg', 'Lớn'], ['xl', 'Rất lớn']], S.size, 'size') + '</div>';
    h += '<section class="card"><div class="t1">Sao lưu &amp; chuyển máy</div>' +
      '<div class="t2" style="line-height:1.5">Xuất ' + reminders.length + ' nhắc lịch ra một file để cất giữ hoặc chuyển sang điện thoại khác (iPhone hay Android đều được). Trên máy mới, mở ứng dụng và chọn <b>Nhập từ file</b>.</div>' +
      '<div class="g2"><button class="primary" data-act="backup-export" style="height:48px">Xuất file sao lưu</button>' +
      '<label class="secondary" for="backup-file" role="button" tabindex="0">Nhập từ file</label></div>' +
      '<input type="file" id="backup-file" accept=".json,application/json,text/plain" hidden>' +
      '<div class="mut">Nhập sẽ thêm vào danh sách hiện có; nhắc lịch trùng sẽ được bỏ qua.</div></section>';
    if (isIos() && !isStandalone()) {
      h += '<section class="card"><div class="t1">Cài lên màn hình chính iPhone</div><div class="t2" style="line-height:1.5">1. Mở trang này bằng <b>Safari</b>.<br>2. Bấm nút <b>Chia sẻ</b> (hình vuông có mũi tên lên).<br>3. Chọn <b>“Thêm vào MH chính”</b> rồi bấm <b>Thêm</b>.<br>Ứng dụng sẽ có biểu tượng riêng và dùng được khi không có mạng.</div></section>';
    }
    h += '<section class="card"><div class="row"><div class="col" style="flex:1"><span class="label" style="letter-spacing:1px">NHÀ PHÁT TRIỂN</span><span class="mut">Lịch Âm Dương · Phiên bản 1.0.0</span></div>' +
      '<div class="logo"><img src="icons/itd-logo.png" alt="Logo ITD Vietnam"></div></div>' +
      '<div class="t1">Công ty cổ phần Công nghệ ITD Việt Nam</div>' +
      '<a class="contact" href="tel:0988095867"><span class="ci">' + I.phone + '</span><span class="col"><span class="mut">Điện thoại / Zalo</span><b>0988095867</b></span></a>' +
      '<a class="contact" href="mailto:vanthu.itdvn@gmail.com"><span class="ci">' + I.mail + '</span><span class="col"><span class="mut">Email</span><b>vanthu.itdvn@gmail.com</b></span></a>' +
      '<a class="contact" href="https://itdvietnam.com.vn" target="_blank" rel="noopener"><span class="ci">' + I.web + '</span><span class="col"><span class="mut">Website</span><b>itdvietnam.com.vn</b></span></a>' +
      '<a class="contact" href="https://maps.apple.com/?q=' + encodeURIComponent('Số 1 Ngõ 30 đường Lý Sơn, Hà Nội') + '" target="_blank" rel="noopener"><span class="ci">' + I.pin + '</span><span class="col"><span class="mut">Địa chỉ</span><b>Số 1 Ngõ 30 đường Lý Sơn, Hà Nội</b></span></a>' +
      '<div class="g2"><a class="primary" href="tel:0988095867" style="height:48px">Gọi hỗ trợ</a><a class="secondary" href="https://zalo.me/0988095867" target="_blank" rel="noopener">Nhắn Zalo</a></div></section>' +
      '<div class="foot">© 2026 Công ty cổ phần Công nghệ ITD Việt Nam</div></div>';
    return h;
  }

  // ---------- Bố cục ----------
  var mq = window.matchMedia('(min-width: 700px)');
  function tabs() {
    var items = S.wide
      ? [['month', 'Lịch', I.cal], ['convert', 'Đổi ngày', I.conv], ['reminders', 'Nhắc lịch', I.bell], ['settings', 'Cài đặt', I.set]]
      : [['month', 'Tháng', I.cal], ['day', 'Ngày', I.day], ['convert', 'Đổi ngày', I.conv], ['reminders', 'Nhắc lịch', I.bell]];
    return '<nav class="tabbar" aria-label="Điều hướng chính">' + items.map(function (it) {
      var on = S.dest === it[0] || (S.wide && it[0] === 'month' && S.dest === 'day');
      return '<button class="tab" data-act="go" data-arg="' + it[0] + '"' + (on ? ' aria-current="page"' : '') + '>' + it[2] + it[1] + '</button>';
    }).join('') + '</nav>';
  }
  function panes() {
    if (!S.wide) {
      var v = S.editing ? viewEditor() : S.dest === 'day' ? viewDay() : S.dest === 'convert' ? viewConvert()
        : S.dest === 'reminders' ? viewReminders(null) : S.dest === 'settings' ? viewSettings() : viewMonth(false);
      return '<section class="pane" id="p1">' + v + '</section>';
    }
    if (S.dest === 'settings') return '<section class="pane" id="p1"><div style="max-width:640px;margin:0 auto">' + viewSettings() + '</div></section>';
    var left, right;
    if (S.dest === 'convert') { left = viewConvert(); right = viewReminders(null); }
    else if (S.dest === 'reminders') {
      left = viewReminders(S.editing ? S.editing.id : null);
      right = S.editing ? viewEditor() : '<div class="empty"><p>Chọn một nhắc lịch bên trái để sửa, hoặc tạo nhắc mới.</p><button class="primary" data-act="new" style="max-width:260px;margin:16px auto 0">Tạo nhắc lịch</button></div>';
    } else { left = viewMonth(true); right = viewDay(); }
    return '<section class="pane left" id="p1">' + left + '</section><section class="pane right" id="p2">' + right + '</section>';
  }
  function render() {
    S.wide = mq.matches;
    var keep = {};
    ['p1', 'p2'].forEach(function (id) { var el = document.getElementById(id); if (el) keep[id] = el.scrollTop; });
    var showTabs = !(S.editing && !S.wide);
    app.innerHTML = '<div class="shell"><main class="panes">' + panes() + '</main>' + (showTabs ? tabs() : '') + '</div>';
    Object.keys(keep).forEach(function (id) { var el = document.getElementById(id); if (el) el.scrollTop = keep[id]; });
    bindSwipe();
  }
  function go(dest, resetScroll) {
    S.dest = dest;
    render();
    if (resetScroll !== false) ['p1', 'p2'].forEach(function (id) { var el = document.getElementById(id); if (el) el.scrollTop = 0; });
  }

  function bindSwipe() {
    var g = document.getElementById('month-grid'); if (!g) return;
    var x0 = null, y0 = null;
    g.addEventListener('touchstart', function (e) { x0 = e.touches[0].clientX; y0 = e.touches[0].clientY; }, { passive: true });
    g.addEventListener('touchend', function (e) {
      if (x0 == null) return;
      var dx = e.changedTouches[0].clientX - x0, dy = e.changedTouches[0].clientY - y0; x0 = null;
      if (Math.abs(dx) > 60 && Math.abs(dx) > Math.abs(dy) * 1.5) shiftMonth(dx > 0 ? -1 : 1);
    }, { passive: true });
  }
  function shiftMonth(n) {
    var m = S.vm.m + n, y = S.vm.y;
    if (m < 1) { m = 12; y--; } if (m > 12) { m = 1; y++; }
    S.vm = { y: y, m: m }; render();
  }
  function select(jd) { S.sel = jd; var s = L.jdToDate(jd); S.vm = { y: s.y, m: s.m }; }

  function toast(msg) {
    var t = document.createElement('div'); t.className = 'toast'; t.setAttribute('role', 'status'); t.textContent = msg;
    document.body.appendChild(t); setTimeout(function () { t.remove(); }, 2600);
  }

  function newReminder(cal, d, m, y, rep) {
    S.editing = { id: 0, title: '', cal: cal || 'lunar', rep: rep || 'year', d: d || 1, m: m || 1, y: y || L.jdToDate(todayJd()).y, extra: [], before: 1, hour: 7, minute: 0, on: true, alarm: false, aBefore: 0, aHour: 7, aMinute: 0, snooze: 10 };
    S.dest = 'reminders';
  }

  // ---------- Xuất sang ứng dụng Lịch (iPhone/Android/Google) ----------
  function icsDate(jd, h, mi) { var s = L.jdToDate(jd); return s.y + pad(s.m) + pad(s.d) + 'T' + pad(h) + pad(mi) + '00'; }
  function icsEscape(s) { return String(s).replace(/\\/g, '\\\\').replace(/;/g, '\\;').replace(/,/g, '\\,').replace(/\n/g, '\\n'); }
  function ringTrigger(r) {
    var mins = -(r.aBefore | 0) * 1440 + (r.aHour * 60 + r.aMinute) - (r.hour * 60 + r.minute);
    return (mins < 0 ? '-' : '') + 'PT' + Math.abs(mins) + 'M';
  }
  function icsEvents(r) {
    if (!r.on) return [];
    var count = r.rep === 'once' ? 1 : (r.rep === 'month' ? 24 * Math.max(1, 1 + (r.extra || []).length) : 20);
    return L.occurrences(r, todayJd(), count).map(function (j) {
      var l = L.solarToLunar(j), end = r.hour * 60 + r.minute + 30;
      return ['BEGIN:VEVENT', 'UID:lad-' + r.id + '-' + j + '@itdvietnam.com.vn', 'DTSTAMP:' + icsDate(todayJd(), 0, 0) + 'Z',
        'DTSTART:' + icsDate(j, r.hour, r.minute), 'DTEND:' + icsDate(j, Math.min(23, Math.floor(end / 60)), end >= 24 * 60 ? 59 : end % 60),
        'SUMMARY:' + icsEscape(r.title), 'DESCRIPTION:' + icsEscape(L.lunarText(l) + ' âm lịch · ' + ruleText(r)),
        'BEGIN:VALARM', 'ACTION:DISPLAY', 'DESCRIPTION:' + icsEscape(r.title), 'TRIGGER:' + (r.before ? '-P' + r.before + 'D' : 'PT0M'), 'END:VALARM',
        (r.alarm && !ringSame(r) ? 'BEGIN:VALARM\r\nACTION:AUDIO\r\nTRIGGER:' + ringTrigger(r) + '\r\nEND:VALARM' : null),
        'END:VEVENT'].filter(Boolean).join('\r\n');
    });
  }
  function exportIcs(list, name) {
    var ev = []; list.forEach(function (r) { ev = ev.concat(icsEvents(r)); });
    if (!ev.length) { toast('Không có lần nhắc nào sắp tới để thêm.'); return; }
    var ics = ['BEGIN:VCALENDAR', 'VERSION:2.0', 'PRODID:-//ITD Viet Nam//Lich Am Duong//VI', 'CALSCALE:GREGORIAN', 'METHOD:PUBLISH',
      'X-WR-CALNAME:Lịch Âm Dương'].concat(ev).concat(['END:VCALENDAR']).join('\r\n');
    var file = new File([ics], name + '.ics', { type: 'text/calendar' });
    if (navigator.canShare && navigator.canShare({ files: [file] }) && isStandalone()) {
      navigator.share({ files: [file], title: 'Lịch Âm Dương' }).catch(function () {});
      return;
    }
    var a = document.createElement('a');
    a.href = 'data:text/calendar;charset=utf-8,' + encodeURIComponent(ics);
    a.download = name + '.ics';
    document.body.appendChild(a); a.click(); a.remove();
    toast('Đã tạo ' + ev.length + ' lần nhắc. Chọn “Thêm tất cả” trong ứng dụng Lịch.');
  }

  // ---------- Sao lưu & chuyển máy (định dạng chung với bản Android) ----------
  function backupKey(r) { return [r.title, r.cal, r.rep, r.d, r.rep === 'month' ? '' : r.m, r.rep === 'once' ? r.y : '', (r.extra || []).join('+')].join('|'); }
  function exportBackup() {
    var data = {
      app: 'lich-am-duong', format: 1, exportedAt: new Date().toISOString(),
      reminders: reminders.map(function (r) {
        return { title: r.title, cal: r.cal, rep: r.rep, d: r.d, m: r.m, y: r.y, extra: r.extra || [], before: r.before, hour: r.hour, minute: r.minute, on: r.on, alarm: !!r.alarm,
          alarmBefore: r.alarm ? (r.aBefore | 0) : r.before, alarmHour: r.alarm ? r.aHour : r.hour, alarmMinute: r.alarm ? r.aMinute : r.minute, snooze: r.snooze || 10 };
      })
    };
    var s = L.jdToDate(todayJd()), name = 'lich-am-duong-sao-luu-' + s.y + pad(s.m) + pad(s.d) + '.json';
    var text = JSON.stringify(data, null, 2), file = new File([text], name, { type: 'application/json' });
    if (navigator.canShare && navigator.canShare({ files: [file] })) {
      navigator.share({ files: [file], title: 'Sao lưu Lịch Âm Dương' }).then(function () { toast('Đã xuất ' + reminders.length + ' nhắc lịch'); }).catch(function () {});
      return;
    }
    var url = URL.createObjectURL(file), a = document.createElement('a');
    a.href = url; a.download = name; document.body.appendChild(a); a.click(); a.remove();
    setTimeout(function () { URL.revokeObjectURL(url); }, 4000);
    toast('Đã lưu file ' + name);
  }
  function importBackup(text) {
    var data = JSON.parse(text);
    if (!data || data.app !== 'lich-am-duong' || !Array.isArray(data.reminders)) throw new Error('bad');
    var keys = reminders.map(backupKey), nextId = reminders.reduce(function (m, x) { return Math.max(m, x.id); }, 0) + 1, added = 0, skipped = 0;
    data.reminders.forEach(function (x) {
      if (!x || (x.cal !== 'lunar' && x.cal !== 'solar') || ['once', 'month', 'year'].indexOf(x.rep) < 0) { skipped++; return; }
      var r = {
        id: 0, title: String(x.title || 'Nhắc lịch').slice(0, 120), cal: x.cal, rep: x.rep,
        d: clamp(parseInt(x.d, 10) || 1, 1, x.cal === 'lunar' ? 30 : 31), m: clamp(parseInt(x.m, 10) || 1, 1, 12), y: clamp(parseInt(x.y, 10) || 2026, 1900, 2100),
        extra: (Array.isArray(x.extra) ? x.extra : []).map(function (v) { return clamp(parseInt(v, 10) || 1, 1, 31); }).slice(0, 1),
        before: clamp(parseInt(x.before, 10) || 0, 0, 30),
        hour: clamp(parseInt(x.hour, 10) || 0, 0, 23), minute: clamp(parseInt(x.minute, 10) || 0, 0, 59), on: x.on !== false, alarm: x.alarm === true
      };
      r.aBefore = x.alarmBefore != null ? clamp(parseInt(x.alarmBefore, 10) || 0, 0, 30) : r.before;
      r.aHour = x.alarmHour != null ? clamp(parseInt(x.alarmHour, 10) || 0, 0, 23) : r.hour;
      r.aMinute = x.alarmMinute != null ? clamp(parseInt(x.alarmMinute, 10) || 0, 0, 59) : r.minute;
      r.snooze = [5, 10, 15, 30].indexOf(+x.snooze) >= 0 ? +x.snooze : 10;
      var k = backupKey(r);
      if (keys.indexOf(k) >= 0) { skipped++; return; }
      r.id = nextId++; reminders.push(r); keys.push(k); added++;
    });
    saveReminders();
    return { added: added, skipped: skipped };
  }

  // ---------- Xử lý thao tác ----------
  var A = {
    go: function (a) { S.editing = null; go(a); },
    'go-month': function () { go('month'); },
    sel: function (a) { select(+a); render(); },
    today: function () { select(todayJd()); render(); },
    'm-prev': function () { shiftMonth(-1); },
    'm-next': function () { shiftMonth(1); },
    'd-step': function (a) { select(S.sel + (+a)); render(); },
    'd-remind': function () { var l = L.solarToLunar(S.sel); newReminder('lunar', l.d, l.m, l.y, 'year'); go('reminders'); },
    'cv-mode': function (a) { S.conv.mode = a; render(); },
    'cv-step': function (a) {
      var p = a.split(':'), f = p[0], n = +p[1], c = S.conv;
      if (f === 'sd') c.sd = wrap(Math.min(c.sd, L.dim(c.sm, c.sy)) + n, 1, L.dim(c.sm, c.sy));
      if (f === 'sm') c.sm = wrap(c.sm + n, 1, 12);
      if (f === 'sy') c.sy = clamp(c.sy + n, 1900, 2100);
      if (f === 'ld') c.ld = wrap(c.ld + n, 1, 30);
      if (f === 'lm') c.lm = wrap(c.lm + n, 1, 12);
      if (f === 'ly') c.ly = clamp(c.ly + n, 1900, 2100);
      render();
    },
    'cv-leap': function () { S.conv.leap = !S.conv.leap; render(); },
    'cv-swap': function () {
      var c = S.conv, r = convResult();
      if (r.ok) {
        if (c.mode === 's2l') { c.ld = r.l.d; c.lm = r.l.m; c.ly = r.l.y; c.leap = r.l.leap; }
        else { var s = L.jdToDate(r.jd); c.sd = s.d; c.sm = s.m; c.sy = s.y; }
      }
      c.mode = c.mode === 's2l' ? 'l2s' : 's2l'; render();
    },
    'cv-remind': function () { var r = convResult(); if (r.ok) { newReminder('lunar', r.l.d, r.l.m, r.l.y, 'year'); go('reminders'); } },
    filter: function (a) { S.filter = a; render(); },
    new: function () { newReminder(); go('reminders', false); },
    'rem-open': function (a) {
      var r = reminders.filter(function (x) { return x.id === +a; })[0];
      if (r) { S.editing = JSON.parse(JSON.stringify(r)); S.dest = 'reminders'; render(); }
    },
    'rem-toggle': function (a) { reminders.forEach(function (x) { if (x.id === +a) x.on = !x.on; }); saveReminders(); render(); },
    'ics-all': function () { exportIcs(reminders, 'lich-am-duong'); },
    'ed-close': function () { S.editing = null; render(); },
    'ed-cal': function (a) { S.editing.cal = a; render(); },
    'ed-rep': function (a) { S.editing.rep = a; render(); },
    'ed-before': function (a) { S.editing.before = +a; render(); },
    'ed-alarm': function () {
      var e = S.editing;
      // Lần đầu bật chuông: lấy giờ chuông bằng giờ thông báo, sau đó chỉnh tự do
      if (!e.alarm && (e.aHour == null || (!e.id && !e._alarmSet))) { e.aBefore = e.before; e.aHour = e.hour; e.aMinute = e.minute; e._alarmSet = true; }
      e.alarm = !e.alarm; render();
    },
    'ed-snooze': function (a) { S.editing.snooze = +a; render(); },
    'ed-days': function (a) {
      var p = a.split(':'), e = S.editing, k = p[0] === 'a' ? 'aBefore' : 'before';
      e[k] = clamp((e[k] | 0) + (+p[1]), 0, 30); render();
    },
    'ed-extra': function () { var e = S.editing; e.extra = (e.extra && e.extra.length) ? [] : [e.d === 15 ? 1 : 15]; render(); },
    'ed-step': function (a) {
      var p = a.split(':'), f = p[0], n = +p[1], e = S.editing, md = maxDay(e);
      if (f === 'd') e.d = wrap(Math.min(e.d, md) + n, 1, md);
      if (f === 'm') e.m = wrap(e.m + n, 1, 12);
      if (f === 'y') e.y = clamp(e.y + n, L.jdToDate(todayJd()).y - 1, 2100);
      if (f === 'x') e.extra = [wrap(e.extra[0] + n, 1, md)];
      if (f === 'hour') e.hour = wrap(e.hour + n, 0, 23);
      if (f === 'minute') e.minute = (e.minute + (n > 0 ? 15 : 45)) % 60;
      render();
    },
    'ed-save': function () {
      var e = S.editing; e.d = Math.min(e.d, maxDay(e)); e.title = (e.title || '').trim() || 'Nhắc lịch'; delete e._alarmSet;
      var isNew = !e.id;
      if (isNew) { e.id = reminders.reduce(function (m, x) { return Math.max(m, x.id); }, 0) + 1; reminders.push(e); }
      else reminders = reminders.map(function (x) { return x.id === e.id ? e : x; });
      saveReminders(); S.editing = null; render();
      var saved = reminders.filter(function (x) { return x.id === e.id; })[0];
      if (isIos()) {
        if (window.confirm('Đã lưu “' + saved.title + '”.\nThêm các lần nhắc vào ứng dụng Lịch của iPhone để được báo đúng giờ?')) exportIcs([saved], 'nhac-' + saved.id);
      } else toast('Đã lưu nhắc lịch');
    },
    'ed-del': function () {
      var e = S.editing;
      if (!window.confirm('Xoá nhắc lịch “' + e.title + '”?')) return;
      reminders = reminders.filter(function (x) { return x.id !== e.id; }); saveReminders(); S.editing = null; render();
    },
    'backup-export': function () {
      if (!reminders.length) { toast('Chưa có nhắc lịch nào để xuất.'); return; }
      exportBackup();
    },
    theme: function (a) { S.theme = a; store.set('theme', a); applyPrefs(); render(); },
    size: function (a) { S.size = a; store.set('size', a); applyPrefs(); render(); }
  };

  app.addEventListener('click', function (e) {
    var el = e.target.closest('[data-act]');
    if (!el || el.disabled) return;
    var fn = A[el.getAttribute('data-act')];
    if (fn) { e.preventDefault(); fn(el.getAttribute('data-arg')); }
  });
  app.addEventListener('change', function (e) {
    var tk = e.target.getAttribute && e.target.getAttribute('data-time');
    if (tk && S.editing && /^\d{2}:\d{2}/.test(e.target.value)) {
      var hm = e.target.value.split(':').map(Number);
      if (tk === 'a') { S.editing.aHour = hm[0]; S.editing.aMinute = hm[1]; } else { S.editing.hour = hm[0]; S.editing.minute = hm[1]; }
      render(); return;
    }
    if (e.target.id !== 'backup-file' || !e.target.files || !e.target.files[0]) return;
    var reader = new FileReader();
    reader.onload = function () {
      try {
        var res = importBackup(String(reader.result));
        render();
        toast('Đã nhập ' + res.added + ' nhắc lịch' + (res.skipped ? ', bỏ qua ' + res.skipped + ' nhắc trùng' : '') + '.');
        if (res.added && isIos() && window.confirm('Thêm các nhắc lịch vào ứng dụng Lịch của iPhone để được báo đúng giờ?')) exportIcs(reminders, 'lich-am-duong');
      } catch (err) { toast('File không đúng định dạng sao lưu của Lịch Âm Dương.'); }
    };
    reader.readAsText(e.target.files[0]);
    e.target.value = '';
  });
  app.addEventListener('keydown', function (e) {
    if ((e.key === 'Enter' || e.key === ' ') && e.target.getAttribute('for') === 'backup-file') { e.preventDefault(); document.getElementById('backup-file').click(); }
  });
  app.addEventListener('input', function (e) {
    if (e.target.id === 'ed-title' && S.editing) S.editing.title = e.target.value;
  });
  mq.addEventListener ? mq.addEventListener('change', render) : mq.addListener(render);
  // Qua nửa đêm khi đang mở ứng dụng: cập nhật "hôm nay"
  document.addEventListener('visibilitychange', function () { if (!document.hidden) render(); });

  applyPrefs();
  render();

  if ('serviceWorker' in navigator) {
    window.addEventListener('load', function () { navigator.serviceWorker.register('sw.js').catch(function () {}); });
  }
})();

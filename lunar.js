/* Lõi lịch âm Việt Nam (thuật toán Hồ Ngọc Đức, múi giờ +7) — bản JavaScript của lõi Kotlin trong ứng dụng Android. */
(function (global) {
  'use strict';
  var PI = Math.PI, TZ = 7;
  function INT(d) { return Math.floor(d); }

  function jdFromDate(dd, mm, yy) {
    var a = INT((14 - mm) / 12), y = yy + 4800 - a, m = mm + 12 * a - 3;
    var jd = dd + INT((153 * m + 2) / 5) + 365 * y + INT(y / 4) - INT(y / 100) + INT(y / 400) - 32045;
    if (jd < 2299161) jd = dd + INT((153 * m + 2) / 5) + 365 * y + INT(y / 4) - 32083;
    return jd;
  }
  function jdToDate(jd) {
    var a, b, c;
    if (jd > 2299160) { a = jd + 32044; b = INT((4 * a + 3) / 146097); c = a - INT((b * 146097) / 4); }
    else { b = 0; c = jd + 32082; }
    var d = INT((4 * c + 3) / 1461), e = c - INT((1461 * d) / 4), m = INT((5 * e + 2) / 153);
    return { d: e - INT((153 * m + 2) / 5) + 1, m: m + 3 - 12 * INT(m / 10), y: b * 100 + d - 4800 + INT(m / 10) };
  }
  function newMoon(k) {
    var T = k / 1236.85, T2 = T * T, T3 = T2 * T, dr = PI / 180;
    var Jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * T2 - 0.000000155 * T3;
    Jd1 += 0.00033 * Math.sin((166.56 + 132.87 * T - 0.009173 * T2) * dr);
    var M = 359.2242 + 29.10535608 * k - 0.0000333 * T2 - 0.00000347 * T3;
    var Mpr = 306.0253 + 385.81691806 * k + 0.0107306 * T2 + 0.00001236 * T3;
    var F = 21.2964 + 390.67050646 * k - 0.0016528 * T2 - 0.00000239 * T3;
    var C1 = (0.1734 - 0.000393 * T) * Math.sin(M * dr) + 0.0021 * Math.sin(2 * dr * M);
    C1 = C1 - 0.4068 * Math.sin(Mpr * dr) + 0.0161 * Math.sin(dr * 2 * Mpr);
    C1 -= 0.0004 * Math.sin(dr * 3 * Mpr);
    C1 = C1 + 0.0104 * Math.sin(dr * 2 * F) - 0.0051 * Math.sin(dr * (M + Mpr));
    C1 = C1 - 0.0074 * Math.sin(dr * (M - Mpr)) + 0.0004 * Math.sin(dr * (2 * F + M));
    C1 = C1 - 0.0004 * Math.sin(dr * (2 * F - M)) - 0.0006 * Math.sin(dr * (2 * F + Mpr));
    C1 = C1 + 0.0010 * Math.sin(dr * (2 * F - Mpr)) + 0.0005 * Math.sin(dr * (2 * Mpr + M));
    var dt = T < -11 ? 0.001 + 0.000839 * T + 0.0002261 * T2 - 0.00000845 * T3 - 0.000000081 * T * T3
      : -0.000278 + 0.000265 * T + 0.000262 * T2;
    return Jd1 + C1 - dt;
  }
  function nmDay(k) { return INT(newMoon(k) + 0.5 + TZ / 24); }
  function sunLongDeg(jdn) {
    var T = (jdn - 2451545.5 - TZ / 24) / 36525, T2 = T * T, dr = PI / 180;
    var M = 357.52910 + 35999.05030 * T - 0.0001559 * T2 - 0.00000048 * T * T2;
    var L0 = 280.46645 + 36000.76983 * T + 0.0003032 * T2;
    var DL = (1.914600 - 0.004817 * T - 0.000014 * T2) * Math.sin(dr * M) + (0.019993 - 0.000101 * T) * Math.sin(dr * 2 * M) + 0.000290 * Math.sin(dr * 3 * M);
    var L = (L0 + DL) % 360; return L < 0 ? L + 360 : L;
  }
  function sunLong(jdn) {
    var T = (jdn - 2451545.5 - TZ / 24) / 36525, T2 = T * T, dr = PI / 180;
    var M = 357.52910 + 35999.05030 * T - 0.0001559 * T2 - 0.00000048 * T * T2;
    var L0 = 280.46645 + 36000.76983 * T + 0.0003032 * T2;
    var DL = (1.914600 - 0.004817 * T - 0.000014 * T2) * Math.sin(dr * M) + (0.019993 - 0.000101 * T) * Math.sin(dr * 2 * M) + 0.000290 * Math.sin(dr * 3 * M);
    var L = (L0 + DL) * dr; L = L - PI * 2 * INT(L / (PI * 2));
    return INT(L / PI * 6);
  }
  function m11(yy) {
    var off = jdFromDate(31, 12, yy) - 2415021, k = INT(off / 29.530588853), nm = nmDay(k);
    if (sunLong(nm) >= 9) nm = nmDay(k - 1);
    return nm;
  }
  function leapOff(a11) {
    var k = INT((a11 - 2415021.076998695) / 29.530588853 + 0.5), last, i = 1, arc = sunLong(nmDay(k + i));
    do { last = arc; i++; arc = sunLong(nmDay(k + i)); } while (arc !== last && i < 14);
    return i - 1;
  }
  function s2lJd(dn) {
    var k = INT((dn - 2415021.076998695) / 29.530588853), ms = nmDay(k + 1);
    if (ms > dn) ms = nmDay(k);
    var yy = jdToDate(dn).y, a11 = m11(yy), b11 = a11, ly;
    if (a11 >= ms) { ly = yy; a11 = m11(yy - 1); } else { ly = yy + 1; b11 = m11(yy + 1); }
    var ld = dn - ms + 1, diff = INT((ms - a11) / 29), leap = false, lm = diff + 11;
    if (b11 - a11 > 365) { var lo = leapOff(a11); if (diff >= lo) { lm = diff + 10; if (diff === lo) leap = true; } }
    if (lm > 12) lm -= 12;
    if (lm >= 11 && diff < 4) ly -= 1;
    return { d: ld, m: lm, y: ly, leap: leap, start: ms };
  }
  function l2sJd(ld, lm, ly, leap) {
    var a11, b11;
    if (lm < 11) { a11 = m11(ly - 1); b11 = m11(ly); } else { a11 = m11(ly); b11 = m11(ly + 1); }
    var k = INT(0.5 + (a11 - 2415021.076998695) / 29.530588853), off = lm - 11;
    if (off < 0) off += 12;
    if (b11 - a11 > 365) {
      var lo = leapOff(a11), lpm = lo - 2; if (lpm < 0) lpm += 12;
      if (leap && lm !== lpm) return 0;
      if (leap || off >= lo) off += 1;
    } else if (leap) return 0;
    return nmDay(k + off) + ld - 1;
  }
  function monthLen(ms) { return s2lJd(ms + 29).d === 30 ? 30 : 29; }
  function leapMonthOf(ly) { for (var m = 1; m <= 12; m++) { if (l2sJd(1, m, ly, true)) return m; } return 0; }
  function lunarToSolar(d, m, y, leap) {
    var ms = l2sJd(1, m, y, !!leap); if (!ms) return null;
    if (d > monthLen(ms)) return null;
    return ms + d - 1;
  }

  var CAN = ['Giáp', 'Ất', 'Bính', 'Đinh', 'Mậu', 'Kỷ', 'Canh', 'Tân', 'Nhâm', 'Quý'];
  var CHI = ['Tý', 'Sửu', 'Dần', 'Mão', 'Thìn', 'Tỵ', 'Ngọ', 'Mùi', 'Thân', 'Dậu', 'Tuất', 'Hợi'];
  var MONTH = ['', 'Giêng', 'Hai', 'Ba', 'Tư', 'Năm', 'Sáu', 'Bảy', 'Tám', 'Chín', 'Mười', 'Mười Một', 'Chạp'];
  var WEEKDAY = ['Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy', 'Chủ Nhật'];
  var WD_SHORT = ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'];
  var TIET = ['Xuân phân', 'Thanh minh', 'Cốc vũ', 'Lập hạ', 'Tiểu mãn', 'Mang chủng', 'Hạ chí', 'Tiểu thử', 'Đại thử', 'Lập thu', 'Xử thử', 'Bạch lộ', 'Thu phân', 'Hàn lộ', 'Sương giáng', 'Lập đông', 'Tiểu tuyết', 'Đại tuyết', 'Đông chí', 'Tiểu hàn', 'Đại hàn', 'Lập xuân', 'Vũ thủy', 'Kinh trập'];
  var HD = [[0, 1, 3, 6, 8, 9], [2, 3, 5, 8, 10, 11], [0, 1, 4, 5, 7, 10], [0, 2, 3, 6, 7, 9], [2, 4, 5, 8, 9, 11], [1, 4, 6, 7, 10, 11]];
  var SOLAR_EV = { '1/1': 'Tết Dương lịch', '3/2': 'Ngày thành lập Đảng Cộng sản Việt Nam', '14/2': 'Lễ Tình nhân', '8/3': 'Ngày Quốc tế Phụ nữ', '30/4': 'Ngày Giải phóng miền Nam', '1/5': 'Ngày Quốc tế Lao động', '19/5': 'Ngày sinh Chủ tịch Hồ Chí Minh', '1/6': 'Ngày Quốc tế Thiếu nhi', '27/7': 'Ngày Thương binh – Liệt sĩ', '2/9': 'Quốc khánh', '10/10': 'Ngày Giải phóng Thủ đô', '13/10': 'Ngày Doanh nhân Việt Nam', '20/10': 'Ngày Phụ nữ Việt Nam', '20/11': 'Ngày Nhà giáo Việt Nam', '22/12': 'Ngày thành lập Quân đội nhân dân Việt Nam', '24/12': 'Lễ Giáng sinh' };
  var LUNAR_EV = { '1/1': 'Tết Nguyên Đán', '2/1': 'Mùng 2 Tết', '3/1': 'Mùng 3 Tết', '15/1': 'Tết Nguyên Tiêu (Rằm tháng Giêng)', '3/3': 'Tết Hàn thực', '10/3': 'Giỗ Tổ Hùng Vương', '15/4': 'Lễ Phật đản', '5/5': 'Tết Đoan ngọ', '15/7': 'Lễ Vu Lan', '15/8': 'Tết Trung thu', '9/9': 'Tết Trùng Cửu', '23/12': 'Ông Công ông Táo' };

  function yearCC(y) { return CAN[(y + 6) % 10] + ' ' + CHI[(y + 8) % 12]; }
  function monthCC(m, y) { return CAN[(y * 12 + m + 3) % 10] + ' ' + CHI[(m + 1) % 12]; }
  function dayCC(jd) { return CAN[(jd + 9) % 10] + ' ' + CHI[(jd + 1) % 12]; }
  function tietIndex(jd) { return INT(sunLongDeg(jd + 1) / 15) % 24; }
  function moon(d) {
    if (d === 1) return 'Trăng non'; if (d <= 6) return 'Trăng lưỡi liềm'; if (d <= 9) return 'Trăng thượng huyền';
    if (d <= 13) return 'Trăng gần tròn'; if (d <= 16) return 'Trăng tròn'; if (d <= 24) return 'Trăng hạ huyền';
    return 'Trăng lưỡi liềm cuối tháng';
  }
  function dayName(d) { return d <= 10 ? 'Mùng ' + d : 'Ngày ' + d; }
  function lunarText(l) { return dayName(l.d) + ' tháng ' + MONTH[l.m] + (l.leap ? ' nhuận' : ''); }
  function lunarShort(l) { return l.d + '/' + l.m + (l.leap ? ' nhuận' : ''); }
  function weekdayIdx(jd) { return jd % 7; } // 0 = Thứ Hai

  function dayInfo(jd) {
    var s = jdToDate(jd), l = s2lJd(jd), len = monthLen(l.start), hi = (jd + 1) % 12, ev = [];
    if (SOLAR_EV[s.d + '/' + s.m]) ev.push(SOLAR_EV[s.d + '/' + s.m]);
    if (!l.leap) {
      if (LUNAR_EV[l.d + '/' + l.m]) ev.push(LUNAR_EV[l.d + '/' + l.m]);
      if (l.m === 12 && l.d === len) ev.push('Giao thừa');
      if (l.d === 15 && [1, 4, 7, 8].indexOf(l.m) < 0) ev.push('Rằm tháng ' + MONTH[l.m]);
    }
    var ti = tietIndex(jd);
    if (ti !== tietIndex(jd - 1)) ev.push('Bắt đầu tiết ' + TIET[ti]);
    return {
      jd: jd, s: s, l: l, len: len, wd: weekdayIdx(jd), weekday: WEEKDAY[weekdayIdx(jd)],
      solarText: s.d + '/' + s.m + '/' + s.y, dayCC: dayCC(jd), monthCC: monthCC(l.m, l.y), yearCC: yearCC(l.y),
      tiet: TIET[ti], moon: moon(l.d), events: ev, special: l.d === 1 || l.d === 15,
      gio: HD[hi % 6].map(function (i) { var st = (23 + 2 * i) % 24; return { chi: CHI[i], range: st + 'h – ' + ((st + 2) % 24) + 'h' }; })
    };
  }

  function dim(m, y) { return new Date(Date.UTC(y, m, 0)).getUTCDate(); }

  /* Các lần nhắc kế tiếp. r = {cal:'lunar'|'solar', rep:'once'|'month'|'year', d, m, y, extra:[...]} */
  function occurrences(r, fromJd, count) {
    var out = [], days = [r.d].concat(r.extra || []).filter(function (v, i, a) { return a.indexOf(v) === i; }).sort(function (a, b) { return a - b; });
    var j, y, ms, g;
    if (r.cal === 'solar') {
      var f = jdToDate(fromJd);
      if (r.rep === 'once') { j = jdFromDate(Math.min(r.d, dim(r.m, r.y)), r.m, r.y); if (j >= fromJd) out.push(j); }
      else if (r.rep === 'year') { for (y = f.y; out.length < count && y < f.y + count + 2; y++) { j = jdFromDate(Math.min(r.d, dim(r.m, y)), r.m, y); if (j >= fromJd) out.push(j); } }
      else {
        var m = f.m; y = f.y;
        for (g = 0; out.length < count && g < count + 2; g++) {
          days.forEach(function (d) { var jj = jdFromDate(Math.min(d, dim(m, y)), m, y); if (jj >= fromJd && out.length < count && out.indexOf(jj) < 0) out.push(jj); });
          m++; if (m > 12) { m = 1; y++; }
        }
      }
    } else {
      if (r.rep === 'month') {
        ms = s2lJd(fromJd).start;
        for (g = 0; out.length < count && g < count + 2; g++) {
          var len = monthLen(ms);
          days.forEach(function (d) { var jj = ms + Math.min(d, len) - 1; if (jj >= fromJd && out.length < count && out.indexOf(jj) < 0) out.push(jj); });
          ms += len;
        }
      } else if (r.rep === 'year') {
        var ly = s2lJd(fromJd).y;
        for (y = ly; out.length < count && y < ly + count + 2; y++) { ms = l2sJd(1, r.m, y, false); if (ms) { j = ms + Math.min(r.d, monthLen(ms)) - 1; if (j >= fromJd) out.push(j); } }
      } else { ms = l2sJd(1, r.m, r.y, false); if (ms) { j = ms + Math.min(r.d, monthLen(ms)) - 1; if (j >= fromJd) out.push(j); } }
    }
    return out;
  }

  global.Lunar = {
    jdFromDate: jdFromDate, jdToDate: jdToDate, solarToLunar: s2lJd, lunarToSolar: lunarToSolar,
    monthLen: monthLen, leapMonthOf: leapMonthOf, dayInfo: dayInfo, occurrences: occurrences, dim: dim,
    yearCC: yearCC, monthCC: monthCC, dayCC: dayCC, lunarText: lunarText, lunarShort: lunarShort, dayName: dayName,
    MONTH: MONTH, WEEKDAY: WEEKDAY, WD_SHORT: WD_SHORT
  };
})(typeof window !== 'undefined' ? window : globalThis);

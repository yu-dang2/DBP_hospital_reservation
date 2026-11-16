/* 와이어프레임 동작 확인용 스크립트 (실제 구현 시 서버 연동으로 대체) */
document.addEventListener('DOMContentLoaded', () => {
  // 요구사항 번호 표시/숨김
  const t = document.getElementById('frToggle');
  if (t) {
    let hidden = false;
    try { hidden = localStorage.getItem('hideFr') === '1'; } catch (e) {}
    const apply = () => { document.body.classList.toggle('hide-fr', hidden); t.textContent = hidden ? '요구사항 번호 보기' : '요구사항 번호 숨기기'; };
    apply();
    t.addEventListener('click', () => { hidden = !hidden; try { localStorage.setItem('hideFr', hidden ? '1' : '0'); } catch (e) {} apply(); });
  }

  // 진료 예약: 날짜·시간 선택
  const summary = document.getElementById('selSummary');
  const slotTitle = document.getElementById('slotTitle');
  const week = ['일','월','화','수','목','금','토'];
  let date = '9월 22일(화)', time = '14:30';
  const render = () => { if (summary) summary.textContent = `2026년 ${date} ${time || '(시간 선택)'} · 신경과 김○○ 교수`; if (slotTitle) slotTitle.textContent = `${date} 예약 가능 시간`; };
  document.querySelectorAll('.cal-day:not(:disabled)').forEach(b => b.addEventListener('click', () => {
    document.querySelectorAll('.cal-day.active').forEach(x => { x.classList.remove('active'); x.setAttribute('aria-pressed','false'); });
    b.classList.add('active'); b.setAttribute('aria-pressed','true');
    const d = +b.dataset.day; date = `9월 ${d}일(${week[new Date(2026, 8, d).getDay()]})`;
    time = ''; document.querySelectorAll('.slot.active').forEach(x => x.classList.remove('active')); render();
  }));
  document.querySelectorAll('.slot:not(:disabled)').forEach(b => b.addEventListener('click', () => {
    document.querySelectorAll('.slot.active').forEach(x => { x.classList.remove('active'); x.setAttribute('aria-pressed','false'); });
    b.classList.add('active'); b.setAttribute('aria-pressed','true'); time = b.textContent.trim(); render();
  }));

  // 로그인 유형 탭
  document.querySelectorAll('.seg button').forEach(b => b.addEventListener('click', () => {
    b.parentElement.querySelectorAll('button').forEach(x => { x.classList.remove('active'); x.setAttribute('aria-selected','false'); });
    b.classList.add('active'); b.setAttribute('aria-selected','true');
    const h = document.getElementById('loginType'); if (h) h.value = b.dataset.type;
  }));

  // 예약 내역 상태 탭 필터
  document.querySelectorAll('.tabs-w button').forEach(b => b.addEventListener('click', () => {
    b.parentElement.querySelectorAll('button').forEach(x => { x.classList.remove('active'); x.setAttribute('aria-selected','false'); });
    b.classList.add('active'); b.setAttribute('aria-selected','true');
    const f = b.dataset.filter;
    document.querySelectorAll('tr[data-status]').forEach(r => { r.hidden = !(f === 'all' || r.dataset.status === f); });
  }));

  // 회원가입: 아이디 중복 확인 (예시)
  const dup = document.getElementById('dupCheck');
  if (dup) dup.addEventListener('click', () => {
    const v = document.getElementById('sid').value.trim(), m = document.getElementById('sidMsg');
    if (!v) { m.textContent = '아이디를 입력해 주세요.'; m.style.color = 'var(--danger)'; return; }
    const taken = v === 'admin';
    m.textContent = taken ? '이미 사용 중인 아이디예요.' : '사용 가능한 아이디예요.';
    m.style.color = taken ? 'var(--danger)' : 'var(--ok)';
  });
  const pw = document.getElementById('spw'), pw2 = document.getElementById('spw2'), pm = document.getElementById('pwMsg');
  if (pw && pw2 && pm) { const chk = () => { pm.textContent = pw2.value && pw.value !== pw2.value ? '비밀번호가 일치하지 않아요.' : ''; }; pw.addEventListener('input', chk); pw2.addEventListener('input', chk); }

  // 로그인: 로그인이 필요한 화면에서 넘어온 경우 안내 표시, 관리자 화면이면 관리자 탭 선택
  const rn = document.getElementById('redirectNotice');
  const redirect = new URLSearchParams(location.search).get('redirect');
  if (rn && redirect) {
    rn.hidden = false;
    if (redirect.startsWith('admin')) document.querySelector('.seg button[data-type="ADMIN"]')?.click();
  }

  // 회원 탈퇴: 동의해야 버튼 활성화
  const agree = document.getElementById('wAgree'), wSubmit = document.getElementById('wSubmit');
  if (agree && wSubmit) agree.addEventListener('change', () => { wSubmit.disabled = !agree.checked; });
});

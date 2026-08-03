import { useCallback, useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { getCookie } from '@/lib/cookie.jsx';
import URL from '@/constants/URL.jsx';
import '@/style/UserBookmarks.css';

// BookmarksInfo.jsx와 동일한 임시 고정값 (사용자 구분별 즐겨찾기 개념이 아직 없음)
const USER_GUBUN = 'ADMIN';

// 경로가 같아도 쿼리스트링(comGubun 등)으로 페이지가 갈리는 메뉴(공급사관리/판매사관리 등)를 구분하기 위해
// item.url에 명시된 쿼리 파라미터만 현재 위치와 비교한다(menuNo 같은 부가 파라미터는 무시).
const isActiveBookmark = (item, location) => {
  if (!item?.url) return false;
  const [itemPath, itemQuery] = item.url.split('?');
  if (location.pathname !== itemPath) return false;
  if (!itemQuery) return true;

  const itemParams = new URLSearchParams(itemQuery);
  const currentParams = new URLSearchParams(location.search);
  for (const [key, value] of itemParams.entries()) {
    if (currentParams.get(key) !== value) return false;
  }
  return true;
};

const RefreshIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path d="M4 4V9H4.582M20 20V15H19.418M4.582 9C5.24585 7.35812 6.43568 5.97595 7.96503 5.07875C9.49438 4.18154 11.2768 3.81865 13.0353 4.04524C14.7939 4.27183 16.4276 5.07569 17.6797 6.32977C18.9318 7.58384 19.7295 9.21873 19.951 10.978M19.418 15C18.7542 16.6419 17.5643 18.0241 16.035 18.9213C14.5056 19.8185 12.7232 20.1814 10.9647 19.9548C9.20611 19.7282 7.57244 18.9243 6.32033 17.6702C5.06822 16.4162 4.27050 14.7813 4.04899 13.022"
      stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
  </svg>
);

const UserBookmarks = () => {
  const userId = getCookie('userId') || '';
  const location = useLocation();
  const navigate = useNavigate();

  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const dragIndexRef = useRef(null);
  const [dragOverIndex, setDragOverIndex] = useState(null);

  const fetchBookmarks = useCallback(async () => {
    if (!userId) return;
    setLoading(true);
    try {
      const res = await fnAjaxFetch({
        url: URL.BOOKMARKS_LIST,
        method: 'POST',
        data: { userId, userGubun: USER_GUBUN },
      });
      setItems(res?.data?.result?.resultList || []);
    } catch {
      // 조회 실패 시에도 화면 전체 흐름을 막지 않도록 조용히 빈 목록 처리
      setItems([]);
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    (async () => { await fetchBookmarks(); })();
  }, [fetchBookmarks]);

  const persistOrder = useCallback(async (nextItems) => {
    const payload = nextItems.map((item, idx) => ({
      userId,
      userGubun: USER_GUBUN,
      progrmFileNm: item.progrmFileNm,
      bookmarksOrder: String(idx + 1),
    }));
    try {
      await fnAjaxFetch({ url: URL.BOOKMARKS_ORDER_UPDATE, method: 'POST', data: payload });
    } catch {
      // 저장 실패 시 서버 기준으로 다시 맞춤
      await fetchBookmarks();
    }
  }, [userId, fetchBookmarks]);

  const handleRemove = useCallback(async (e, progrmFileNm) => {
    e.stopPropagation();
    const prevItems = items;
    setItems((prev) => prev.filter((i) => i.progrmFileNm !== progrmFileNm));
    try {
      await fnAjaxFetch({
        url: URL.BOOKMARKS_DELETE,
        method: 'POST',
        data: [{ userId, userGubun: USER_GUBUN, progrmFileNm }],
      });
    } catch {
      setItems(prevItems);
    }
  }, [items, userId]);

  const handleDragStart = useCallback((idx) => {
    dragIndexRef.current = idx;
  }, []);

  const handleDragEnter = useCallback((idx) => {
    if (dragIndexRef.current === null || dragIndexRef.current === idx) return;
    setDragOverIndex(idx);
  }, []);

  const handleDragOver = useCallback((e) => {
    e.preventDefault();
  }, []);

  const handleDrop = useCallback((idx) => {
    const from = dragIndexRef.current;
    dragIndexRef.current = null;
    setDragOverIndex(null);
    if (from === null || from === idx) return;
    setItems((prev) => {
      const next = [...prev];
      const [moved] = next.splice(from, 1);
      next.splice(idx, 0, moved);
      persistOrder(next);
      return next;
    });
  }, [persistOrder]);

  const handleDragEnd = useCallback(() => {
    dragIndexRef.current = null;
    setDragOverIndex(null);
  }, []);

  if (!userId || (!loading && items.length === 0)) return null;

  return (
    <div className="ub-bar">
      <div className="ub-label">
        <button type="button" className="ub-refresh" onClick={fetchBookmarks} title="새로고침">
          <RefreshIcon />
        </button>
        <span>즐겨찾기 ({items.length})</span>
      </div>
      <div className="ub-list">
        {items.map((item, idx) => {
          const active = isActiveBookmark(item, location);
          return (
            <div
              key={item.progrmFileNm}
              className={`ub-chip ${active ? 'ub-chip-active' : ''} ${dragOverIndex === idx ? 'ub-chip-drag-over' : ''}`}
              draggable
              onDragStart={() => handleDragStart(idx)}
              onDragEnter={() => handleDragEnter(idx)}
              onDragOver={handleDragOver}
              onDrop={() => handleDrop(idx)}
              onDragEnd={handleDragEnd}
              onClick={() => item.url && navigate(item.url)}
            >
              <span className="ub-chip-label">{item.bookmarksName || item.progrmKoreanNm}</span>
              <button
                type="button"
                className="ub-chip-remove"
                onClick={(e) => handleRemove(e, item.progrmFileNm)}
                title="즐겨찾기 삭제"
              >
                ×
              </button>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default UserBookmarks;

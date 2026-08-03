export function computePopupPosition(anchorEl, popupEl, offset = 8) {
    const anchorRect = anchorEl.getBoundingClientRect();
    const popupRect = popupEl.getBoundingClientRect();
    const viewportWidth = window.innerWidth;
    const viewportHeight = window.innerHeight;

    let top = anchorRect.bottom + offset;
    let left = anchorRect.left;

    if (top + popupRect.height > viewportHeight) {
        top = anchorRect.top - popupRect.height - offset;
    }
    if (left + popupRect.width > viewportWidth) {
        left = Math.max(0, viewportWidth - popupRect.width - offset);
    }

    top = Math.max(0, top);
    left = Math.max(0, left);

    return { top, left };
}

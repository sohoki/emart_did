export const calcPenaltyPrice = (productDetailPrice, pentaltyApplyPer) => {
    const price = Number(String(productDetailPrice).replace(/,/g, ''));
    const per = Number(pentaltyApplyPer);
    if (!price || !per) return 0;
    return Math.floor((price * per) / 100);
};

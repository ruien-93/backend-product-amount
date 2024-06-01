package antigravity.service.discount;

public class WonDiscount implements PromotionDiscount {
    @Override
    public int applyDiscount(int originalPrice, int discountValue) {
        return originalPrice - discountValue;
    }
}

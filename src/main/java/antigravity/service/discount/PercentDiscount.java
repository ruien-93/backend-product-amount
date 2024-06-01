package antigravity.service.discount;

public class PercentDiscount implements PromotionDiscount {
    @Override
    public int applyDiscount(int originalPrice, int discountValue) {
        return originalPrice - (originalPrice * discountValue / 100);
    }
}

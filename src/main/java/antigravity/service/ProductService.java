package antigravity.service;

import antigravity.domain.entity.Product;
import antigravity.domain.entity.Promotion;
import antigravity.model.request.ProductInfoRequest;
import antigravity.model.response.ProductAmountResponse;
import antigravity.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository repository;

    public ProductAmountResponse getProductAmount(ProductInfoRequest request) {
        System.out.println("상품 가격 추출 로직을 완성 시켜주세요.");

        //유효한 객체인지
        if (!isRequestValid(request)) {
            return null;
        }else{

            Product product = repository.getProduct(request.getProductId());
            System.out.println(product);


            int originPrice = product.getPrice();
            int discountPrice = 0; // 총 할인 금액

            System.out.println(Arrays.toString(request.getCouponIds()));

            // 프로모션 정보 가져오기
            List<Promotion> promotions = repository.getPromotion(request.getCouponIds());
            System.out.println(promotions);

            Date currentDate = new Date();

            // 프로모션 적용
            for (Promotion promotion : promotions) {

                //promotion 검증
                if (!isPromotionValid(promotion)) {
                    continue;
                }

                // 쿠폰 검증 로직 - 사용가능한 쿠폰만 적용
                if (currentDate.before(promotion.getUse_started_at()) || currentDate.after(promotion.getUse_ended_at())) {
                    continue;
                }

                PromotionDiscount discount = getDiscountPrice(promotion);
                int discountedPrice = discount.applyDiscount(originPrice, promotion.getDiscount_value());
                discountPrice += (originPrice - discountedPrice);

            }


            // 최종 가격 계산
            int finalPrice = originPrice - discountPrice;

            // 최종 가격은 천단위 절삭
            finalPrice = (finalPrice / 1000) * 1000;

            // 최소 및 최대 가격
            if (finalPrice < 10000) {
                finalPrice = 10000;
            } else if (finalPrice > 10000000) {
                finalPrice = 10000000;
            }

            // ProductAmountResponse 객체로 반환
            return new ProductAmountResponse(product.getName(), originPrice, discountPrice, finalPrice);

        }


    }

    private boolean isRequestValid(ProductInfoRequest request) {
        if (request == null) {
            System.out.println("request is null");
            return false;
        }
        if (request.getProductId() <= 0) {
            System.out.println("request.getProductId() is positive");
            return false;
        }
        if (request.getCouponIds() == null || request.getCouponIds().length == 0) {
            System.out.println("request.getCouponIds() is Empty");
            return false;
        }
        return true;
    }

    private boolean isPromotionValid(Promotion promotion) {
        if (promotion.getDiscount_value() <= 0) {
            System.out.println("promotion.getDiscount_value() is not valid");
            return false;
        } else if (!"COUPON".equals(promotion.getPromotion_type()) && !"CODE".equals(promotion.getPromotion_type())) {
            System.out.println("promotion.getPromotion_type() is not valid");
            return false;
        }
        return true;
    }

    private PromotionDiscount getDiscountPrice(Promotion promotion) {
        if ("COUPON".equals(promotion.getPromotion_type())) {
            return new WonDiscount();
        } else if ("CODE".equals(promotion.getPromotion_type())) {
            return new PercentDiscount();
        }
        return null;
    }

    private interface PromotionDiscount {
        int applyDiscount(int originalPrice, int discountValue);
    }

    private class WonDiscount implements PromotionDiscount {
        @Override
        public int applyDiscount(int originalPrice, int discountValue) {
            return originalPrice - discountValue;
        }
    }

    private class PercentDiscount implements PromotionDiscount {
        @Override
        public int applyDiscount(int originalPrice, int discountValue) {
            return originalPrice - (originalPrice * discountValue / 100);
        }
    }

}

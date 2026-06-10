package com.huit.CN_Java.service;

import com.huit.CN_Java.dto.CartItem;
import com.huit.CN_Java.entity.Product;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final String CART_KEY = "CART";

    // Lấy giỏ hàng từ session
    @SuppressWarnings("unchecked")
    public List<CartItem> getCart(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute(CART_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(CART_KEY, cart);
        }
        return cart;
    }

    // Thêm sản phẩm vào giỏ
    public void addToCart(HttpSession session, Product product, int quantity) {
        // Kiểm tra không vượt quá tồn kho
        if (quantity > product.getStock()) {
            throw new RuntimeException("Số lượng vượt quá tồn kho (" + product.getStock() + ")");
        }

        List<CartItem> cart = getCart(session);
        Optional<CartItem> existing = cart.stream()
                .filter(item -> item.getProductId().equals(product.getId()))
                .findFirst();

        if (existing.isPresent()) {
            int newQty = existing.get().getQuantity() + quantity;
            if (newQty > product.getStock()) {
                throw new RuntimeException("Số lượng vượt quá tồn kho (" + product.getStock() + ")");
            }
            existing.get().setQuantity(newQty);
        } else {
            cart.add(new CartItem(
                    product.getId(),
                    product.getName(),
                    product.getThumbnail(),
                    product.getDisplayPrice(),
                    quantity
            ));
        }
    }

    // Cập nhật số lượng
    public void updateQuantity(HttpSession session, Long productId,
                               int quantity, Product product) {
        if (quantity <= 0) {
            removeFromCart(session, productId);
            return;
        }
        if (quantity > product.getStock()) {
            throw new RuntimeException("Số lượng vượt quá tồn kho (" + product.getStock() + ")");
        }
        getCart(session).stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));
    }

    // Xóa sản phẩm khỏi giỏ
    public void removeFromCart(HttpSession session, Long productId) {
        getCart(session).removeIf(item -> item.getProductId().equals(productId));
    }

    // Xóa toàn bộ giỏ
    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_KEY);
    }

    // Tổng tiền
    public double getTotal(HttpSession session) {
        return getCart(session).stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }

    // Số lượng sản phẩm trong giỏ
    public int getCartCount(HttpSession session) {
        return getCart(session).stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}
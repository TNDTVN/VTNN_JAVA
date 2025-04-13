package com.example.vtnn.controller;

import com.example.vtnn.model.CartItems;
import com.example.vtnn.model.Order;
import com.example.vtnn.service.CartItemsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartItemsController {

    @Autowired
    private CartItemsService cartItemsService;

    // Lấy danh sách sản phẩm trong giỏ hàng
    @GetMapping("/{accountID}")
    public ResponseEntity<?> getCartItems(@PathVariable int accountID) {
        try {
            List<CartItems> cartItems = cartItemsService.getCartItemsByAccountId(accountID);
            return new ResponseEntity<>(cartItems, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Thêm sản phẩm vào giỏ hàng
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(
            @RequestParam int accountID,
            @RequestParam int productID,
            @RequestParam int quantity) {
        try {
            CartItems cartItem = cartItemsService.addToCart(accountID, productID, quantity);
            return new ResponseEntity<>(cartItem, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Cập nhật số lượng sản phẩm
    @PutMapping("/update")
    public ResponseEntity<?> updateQuantity(
            @RequestParam int cartItemID,
            @RequestParam int quantity) {
        try {
            CartItems updatedItem = cartItemsService.updateQuantity(cartItemID, quantity);
            if (updatedItem == null) {
                return new ResponseEntity<>("Sản phẩm đã bị xóa do số lượng bằng 0", HttpStatus.OK);
            }
            return new ResponseEntity<>(updatedItem, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Xóa sản phẩm khỏi giỏ hàng
    @DeleteMapping("/remove/{cartItemID}")
    public ResponseEntity<?> removeFromCart(@PathVariable int cartItemID) {
        try {
            cartItemsService.removeFromCart(cartItemID);
            return new ResponseEntity<>("Xóa sản phẩm thành công", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Xóa tất cả sản phẩm trong giỏ hàng
    @DeleteMapping("/clear/{accountID}")
    public ResponseEntity<?> clearCart(@PathVariable int accountID) {
        try {
            cartItemsService.clearCart(accountID);
            return new ResponseEntity<>("Xóa toàn bộ giỏ hàng thành công", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Xác nhận đặt hàng với thông tin giao hàng
    @PostMapping("/checkout/{accountID}")
    public ResponseEntity<?> checkout(
            @PathVariable int accountID,
            @RequestParam String shipAddress,
            @RequestParam String shipCity,
            @RequestParam String shipPostalCode,
            @RequestParam String shipCountry,
            @RequestParam(required = false) String notes) {
        try {
            Order order = cartItemsService.checkout(accountID, shipAddress, shipCity, shipPostalCode, shipCountry, notes);
            return new ResponseEntity<>(order, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
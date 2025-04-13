package com.example.vtnn.service;

import com.example.vtnn.model.CartItems;
import com.example.vtnn.model.Customer;
import com.example.vtnn.model.Order;
import com.example.vtnn.model.OrderDetail;
import com.example.vtnn.model.Product;
import com.example.vtnn.repository.CartItemsRepository;
import com.example.vtnn.repository.CustomerRepository;
import com.example.vtnn.repository.OrderDetailRepository;
import com.example.vtnn.repository.OrderRepository;
import com.example.vtnn.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CartItemsService {

    @Autowired
    private CartItemsRepository cartItemsRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private CustomerRepository customerRepository;

    // Lấy customerID từ accountID
    private int getCustomerIdFromAccountId(int accountID) {
        Optional<Customer> customerOptional = customerRepository.findByAccountID(accountID);
        if (!customerOptional.isPresent()) {
            throw new RuntimeException("Không tìm thấy khách hàng với accountID: " + accountID);
        }
        return customerOptional.get().getCustomerID();
    }

    // Lấy danh sách sản phẩm trong giỏ hàng của khách hàng
    public List<CartItems> getCartItemsByAccountId(int accountID) {
        int customerID = getCustomerIdFromAccountId(accountID);
        return cartItemsRepository.findByCustomerID(customerID);
    }

    // Thêm sản phẩm vào giỏ hàng
    public CartItems addToCart(int accountID, int productID, int quantity) {
        int customerID = getCustomerIdFromAccountId(accountID);

        Optional<Product> productOptional = productRepository.findById(productID);
        if (!productOptional.isPresent()) {
            throw new RuntimeException("Sản phẩm không tồn tại!");
        }

        Product product = productOptional.get();
        if (quantity > product.getUnitsInStock()) {
            throw new RuntimeException("Số lượng sản phẩm không đủ trong kho!");
        }

        Optional<CartItems> existingCartItem = cartItemsRepository.findByCustomerIDAndProductID(customerID, productID);
        CartItems cartItem;
        if (existingCartItem.isPresent()) {
            cartItem = existingCartItem.get();
            int newQuantity = cartItem.getQuantity() + quantity;
            if (newQuantity > product.getUnitsInStock()) {
                throw new RuntimeException("Số lượng sản phẩm trong giỏ hàng vượt quá số lượng tồn kho!");
            }
            cartItem.setQuantity(newQuantity);
            product.setUnitsInStock(product.getUnitsInStock() - quantity);
        } else {
            cartItem = new CartItems();
            cartItem.setCustomerID(customerID);
            cartItem.setProductID(productID);
            cartItem.setProductName(product.getProductName());
            cartItem.setQuantity(quantity);
            cartItem.setUnitPrice(product.getUnitPrice());
            cartItem.setImageUrl(product.getImages().isEmpty() ? null : product.getImages().iterator().next().getImageName());
            product.setUnitsInStock(product.getUnitsInStock() - quantity);
        }
        productRepository.save(product);
        return cartItemsRepository.save(cartItem);
    }

    // Cập nhật số lượng sản phẩm trong giỏ hàng
    public CartItems updateQuantity(int cartItemID, int quantity) {
        Optional<CartItems> cartItemOptional = cartItemsRepository.findById(cartItemID);
        if (!cartItemOptional.isPresent()) {
            throw new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng!");
        }

        CartItems cartItem = cartItemOptional.get();
        Optional<Product> productOptional = productRepository.findById(cartItem.getProductID());
        Product product = productOptional.get();

        int oldQuantity = cartItem.getQuantity();
        int stockDifference = quantity - oldQuantity;

        if (quantity <= 0) {
            cartItemsRepository.delete(cartItem);
            product.setUnitsInStock(product.getUnitsInStock() + oldQuantity);
            productRepository.save(product);
            return null;
        }

        if (stockDifference > product.getUnitsInStock()) {
            throw new RuntimeException("Số lượng vượt quá tồn kho!");
        }

        product.setUnitsInStock(product.getUnitsInStock() - stockDifference);
        cartItem.setQuantity(quantity);
        productRepository.save(product);
        return cartItemsRepository.save(cartItem);
    }

    // Xóa sản phẩm khỏi giỏ hàng
    public void removeFromCart(int cartItemID) {
        Optional<CartItems> cartItemOptional = cartItemsRepository.findById(cartItemID);
        if (!cartItemOptional.isPresent()) {
            throw new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng!");
        }

        CartItems cartItem = cartItemOptional.get();
        Optional<Product> productOptional = productRepository.findById(cartItem.getProductID());
        Product product = productOptional.get();
        product.setUnitsInStock(product.getUnitsInStock() + cartItem.getQuantity());
        productRepository.save(product);
        cartItemsRepository.delete(cartItem);
    }

    // Xóa tất cả sản phẩm trong giỏ hàng
    @Transactional
    public void clearCart(int accountID) {
        int customerID = getCustomerIdFromAccountId(accountID);
        List<CartItems> cartItems = cartItemsRepository.findByCustomerID(customerID);
        for (CartItems item : cartItems) {
            Optional<Product> productOptional = productRepository.findById(item.getProductID());
            Product product = productOptional.get();
            product.setUnitsInStock(product.getUnitsInStock() + item.getQuantity());
            productRepository.save(product);
        }
        cartItemsRepository.deleteByCustomerID(customerID);
    }

    @Transactional
    public Order checkout(int accountID, String shipAddress, String shipCity, String shipPostalCode, String shipCountry, String notes) {
        int customerID = getCustomerIdFromAccountId(accountID);
        Optional<Customer> customerOptional = customerRepository.findByAccountID(accountID);
        if (!customerOptional.isPresent()) {
            throw new RuntimeException("Không tìm thấy khách hàng!");
        }

        Customer customer = customerOptional.get();

        // Kiểm tra thông tin khách hàng
        if (isEmpty(customer.getCustomerName()) || isEmpty(customer.getContactName()) ||
                isEmpty(customer.getAddress()) || isEmpty(customer.getCity()) ||
                isEmpty(customer.getPostalCode()) || isEmpty(customer.getCountry()) ||
                isEmpty(customer.getPhone()) || isEmpty(customer.getEmail())) {
            throw new RuntimeException("Thông tin khách hàng không đầy đủ! Vui lòng cập nhật thông tin cá nhân.");
        }

        // Kiểm tra thông tin giao hàng
        if (isEmpty(shipAddress) || isEmpty(shipCity) || isEmpty(shipPostalCode) || isEmpty(shipCountry)) {
            throw new RuntimeException("Thông tin giao hàng không đầy đủ!");
        }

        List<CartItems> cartItems = cartItemsRepository.findByCustomerID(customerID);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống!");
        }

        // Tạo đơn hàng mới
        Order order = new Order();
        order.setCustomerID(customerID);
        order.setEmployeeID(null);
        order.setOrderDate(new Date());
        order.setShipAddress(shipAddress);
        order.setShipCity(shipCity);
        order.setShipPostalCode(shipPostalCode);
        order.setShipCountry(shipCountry);
        order.setNotes(notes);
        order.setFreight(BigDecimal.ZERO);

        // Lưu Order trước để có orderID
        Order savedOrder = orderRepository.save(order);

        // Tạo và lưu chi tiết đơn hàng
        for (CartItems item : cartItems) {
            Optional<Product> productOptional = productRepository.findById(item.getProductID());
            if (!productOptional.isPresent()) {
                throw new RuntimeException("Sản phẩm không tồn tại!");
            }
            Product product = productOptional.get();
            if (item.getQuantity() > product.getUnitsInStock()) {
                throw new RuntimeException("Số lượng sản phẩm " + product.getProductName() + " không đủ trong kho!");
            }

            OrderDetail detail = new OrderDetail();
            detail.setOrderID(savedOrder.getOrderID());
            detail.setProductID(item.getProductID());
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(item.getUnitPrice());
            detail.setDiscount(0.0f);
            detail.setOrder(savedOrder);
            savedOrder.getOrderDetails().add(detail);

            // Giảm tồn kho khi tạo hóa đơn
            product.setUnitsInStock(product.getUnitsInStock() - item.getQuantity());
            productRepository.save(product);
        }

        // Lưu lại Order với các OrderDetail
        orderRepository.save(savedOrder);
        cartItemsRepository.deleteByCustomerID(customerID);
        return savedOrder;
    }

    // Hàm tiện ích để kiểm tra chuỗi rỗng
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
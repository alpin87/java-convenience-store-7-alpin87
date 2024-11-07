package store.service;

import store.model.Cart;
import store.model.Order;
import store.model.Product;

public class OrderService {
    private final ProductService productService;
    private Cart cart;

    public Cart getCart() {
        return cart;
    }

    public OrderService(ProductService productService) {
        this.productService = productService;
        this.cart = new Cart();
    }

    public void addToCart(String productName, int quantity) {
        Product product = productService.findProduct(productName);
        productService.decreaseStock(productName, quantity);
        cart.addOrder(new Order(product, quantity));
    }
}

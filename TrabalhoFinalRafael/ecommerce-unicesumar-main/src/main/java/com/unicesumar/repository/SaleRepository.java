package com.unicesumar.repository;

import com.unicesumar.entities.Product;
import com.unicesumar.entities.Sale;
import com.unicesumar.entities.User;
import com.unicesumar.paymentMethods.PaymentType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SaleRepository implements EntityRepository<Sale> {
    private final Connection connection;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public SaleRepository(Connection connection) {
        this.connection = connection;
        this.userRepository = new UserRepository(connection);
        this.productRepository = new ProductRepository(connection);
    }

    @Override
    public void save(Sale sale) {
        try {
            String salesQuery = "INSERT INTO sales (id, user_id, payment_method, sale_date) VALUES (?, ?, ?, ?)";
            PreparedStatement salesStmt = connection.prepareStatement(salesQuery);
            salesStmt.setString(1, sale.getUuid().toString());
            salesStmt.setString(2, sale.getUser().getUuid().toString());
            salesStmt.setString(3, sale.getPaymentMethod().toString());
            salesStmt.setTimestamp(4, new Timestamp(sale.getSaleDate().getTime()));
            salesStmt.executeUpdate();

            String saleProductsQuery = "INSERT INTO sale_products (sale_id, product_id) VALUES (?, ?)";
            PreparedStatement saleProductsStmt = connection.prepareStatement(saleProductsQuery);

            for (Product product : sale.getProducts()) {
                saleProductsStmt.setString(1, sale.getUuid().toString());
                saleProductsStmt.setString(2, product.getUuid().toString());
                saleProductsStmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar o pedido", e);
        }
    }

    @Override
    public Optional<Sale> findById(UUID id) {
        String query = "SELECT * FROM sales WHERE id = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, id.toString());
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                UUID userId = UUID.fromString(resultSet.getString("user_id"));
                Optional<User> user = userRepository.findById(userId);

                if (user.isPresent()) {
                    List<Product> products = findProductsBySaleId(id);
                    PaymentType paymentMethod = PaymentType.valueOf(resultSet.getString("payment_method"));

                    Sale sale = new Sale(id, user.get(), products, paymentMethod, resultSet.getTimestamp("sale_date"));

                    return Optional.of(sale);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro de busca pelo ID", e);
        }

        return Optional.empty();
    }

    @Override
    public List<Sale> findAll() {
        String query = "SELECT * FROM sales";
        List<Sale> sales = new ArrayList<>();

        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                UUID id = UUID.fromString(resultSet.getString("id"));
                UUID userId = UUID.fromString(resultSet.getString("user_id"));
                Optional<User> user = userRepository.findById(userId);

                if (user.isPresent()) {
                    List<Product> products = findProductsBySaleId(id);
                    PaymentType paymentMethod = PaymentType.valueOf(resultSet.getString("payment_method"));

                    Sale sale = new Sale(id, user.get(), products, paymentMethod, resultSet.getTimestamp("sale_date"));

                    sales.add(sale);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao achar todos os pedidos", e);
        }

        return sales;
    }

    @Override
    public void deleteById(UUID id) {
        try {
            String deleteProductsQuery = "DELETE FROM sale_products WHERE sale_id = ?";
            PreparedStatement deleteProductsStmt = connection.prepareStatement(deleteProductsQuery);
            deleteProductsStmt.setString(1, id.toString());
            deleteProductsStmt.executeUpdate();

            String deleteSaleQuery = "DELETE FROM sales WHERE id = ?";
            PreparedStatement deleteSaleStmt = connection.prepareStatement(deleteSaleQuery);
            deleteSaleStmt.setString(1, id.toString());
            deleteSaleStmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar o pedido", e);
        }
    }

    private List<Product> findProductsBySaleId(UUID saleId) {
        String query = "SELECT product_id FROM sale_products WHERE sale_id = ?";
        List<Product> products = new ArrayList<>();

        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, saleId.toString());
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                UUID productId = UUID.fromString(resultSet.getString("product_id"));
                Optional<Product> product = productRepository.findById(productId);
                product.ifPresent(products::add);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao achar o pedido", e);
        }

        return products;
    }
}

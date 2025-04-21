package com.unicesumar;

import com.unicesumar.entities.Product;
import com.unicesumar.entities.Sale;
import com.unicesumar.entities.User;
import com.unicesumar.paymentMethods.PaymentType;
import com.unicesumar.repository.ProductRepository;
import com.unicesumar.repository.SaleRepository;
import com.unicesumar.repository.UserRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

public class Main {
    private static ProductRepository productRepository;
    private static UserRepository userRepository;
    private static SaleRepository salesRepository;
    private static Scanner scanner;
    private static PaymentManager paymentManager;

    public static void main(String[] args) {
        Connection conn = null;

        String url = "jdbc:sqlite:database.sqlite";

        try {
            conn = DriverManager.getConnection(url);
            if (conn != null) {
                productRepository = new ProductRepository(conn);
                userRepository = new UserRepository(conn);
                salesRepository = new SaleRepository(conn);
                scanner = new Scanner(System.in);
                paymentManager = new PaymentManager();
            } else {
                System.out.println("Falha na conexão.");
                System.exit(1);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao conectar: " + e.getMessage());
            System.exit(1);
        }

        int option;

        do {
            System.out.println("\n---MENU---");
            System.out.println("1 - Cadastrar Produto");
            System.out.println("2 - Listar Produtos");
            System.out.println("3 - Cadastrar Usuário");
            System.out.println("4 - Listar Usuários");
            System.out.println("5 - Menu de Vendas");
            System.out.println("6 - Sair");
            System.out.println("Escolha uma opção: ");
            option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1:
                    cadastrarProduto();
                    break;
                case 2:
                    listarProdutos();
                    break;
                case 3:
                    cadastrarUsuario();
                    break;
                case 4:
                    listarUsuarios();
                    break;
                case 5:
                    displaySalesMenu();
                    break;
                case 6:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente");
            }

        } while (option != 6);

        scanner.close();
        try {
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void cadastrarProduto() {
        System.out.println("Cadastrar Produto");

        System.out.print("Nome do produto: ");
        String name = scanner.nextLine();

        System.out.print("Preço do produto: ");
        double price = scanner.nextDouble();
        scanner.nextLine(); // Consume newline

        productRepository.save(new Product(name, price));
        System.out.println("Produto cadastrado com sucesso!");
    }

    private static void listarProdutos() {
        System.out.println("Listar Produtos");
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            System.out.println("Não há produtos cadastrados.");
        } else {
            products.forEach(System.out::println);
        }
    }

    private static void cadastrarUsuario() {
        System.out.println("Cadastrar Usuário");

        System.out.print("Nome: ");
        String name = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Senha: ");
        String password = scanner.nextLine();

        userRepository.save(new User(name, email, password));
        System.out.println("Usuário cadastrado com sucesso!");
    }

    private static void listarUsuarios() {
        System.out.println("Listar Usuários");
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            System.out.println("Não há usuários cadastrados.");
        } else {
            users.forEach(System.out::println);
        }
    }

    private static void displaySalesMenu() {
        while (true) {
            System.out.println("\n===== MENU DE VENDAS =====");
            System.out.println("1. Realizar nova venda");
            System.out.println("2. Listar todas as vendas");
            System.out.println("3. Voltar ao menu principal");
            System.out.print("Escolha uma opção: ");

            int option = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (option) {
                case 1:
                    createNewSale();
                    break;
                case 2:
                    listAllSales();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    private static void createNewSale() {
        User user = findUserByEmail();
        if (user == null) {
            System.out.println("Venda cancelada.");
            return;
        }

        List<Product> selectedProducts = selectProductsByIds();
        if (selectedProducts.isEmpty()) {
            System.out.println("Nenhum produto selecionado. Venda cancelada.");
            return;
        }

        double total = displaySaleSummary(user, selectedProducts);

        PaymentType paymentType = selectPaymentMethod();

        System.out.println("\nAguarde, efetuando pagamento...");
        paymentManager.setPaymentMethod(PaymentMethodFactory.create(paymentType));
        String transactionId = paymentManager.pay(total);

        Sale sale = new Sale(user, selectedProducts, paymentType);
        salesRepository.save(sale);

        System.out.println("\nVenda registrada com sucesso!");
    }

    private static User findUserByEmail() {
        System.out.print("\nDigite o Email do usuário: ");
        String email = scanner.nextLine();

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("Usuário encontrado: " + user.getName());
            return user;
        } else {
            System.out.println("Usuário não encontrado para o email: " + email);
            return null;
        }
    }

    private static List<Product> selectProductsByIds() {
        List<Product> allProducts = productRepository.findAll();
        List<Product> selectedProducts = new ArrayList<>();

        if (allProducts.isEmpty()) {
            System.out.println("Não há produtos cadastrados. Cadastre produtos primeiro.");
            return selectedProducts;
        }

        System.out.println("\nProdutos disponíveis:");
        for (Product product : allProducts) {
            System.out.printf("[ID: %s] %s (R$ %.2f)%n", product.getUuid(), product.getName(), product.getPrice());
        }

        System.out.print("\nDigite os IDs dos produtos (separados por vírgula): ");
        String productIdsInput = scanner.nextLine();
        String[] productIdStrings = productIdsInput.split(",");

        System.out.println("Produtos encontrados:");

        for (String idStr : productIdStrings) {
            try {
                UUID productId = UUID.fromString(idStr.trim());
                Optional<Product> productOpt = productRepository.findById(productId);

                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    selectedProducts.add(product);
                    System.out.printf("- [ID: %s] %s (R$ %.2f)%n", product.getUuid(), product.getName(), product.getPrice());
                } else {
                    System.out.println("Produto com ID " + idStr.trim() + " não encontrado.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("ID de produto inválido: " + idStr.trim());
            }
        }

        return selectedProducts;
    }

    private static double displaySaleSummary(User user, List<Product> products) {
        double total = 0;

        System.out.println("\nResumo da venda:");
        System.out.println("Cliente: " + user.getName());
        System.out.println("Produtos:");

        for (Product product : products) {
            System.out.printf("- [ID: %s] %s (R$ %.2f)%n", product.getUuid(), product.getName(), product.getPrice());
            total += product.getPrice();
        }

        System.out.printf("Valor total: R$ %.2f%n", total);
        return total;
    }

    private static PaymentType selectPaymentMethod() {
        System.out.println("\nEscolha a forma de pagamento:");
        System.out.println("1 - Cartão de Crédito");
        System.out.println("2 - Boleto");
        System.out.println("3 - PIX");

        System.out.print("Opção: ");
        int option = scanner.nextInt();
        scanner.nextLine();

        switch (option) {
            case 1:
                return PaymentType.CARTAO;
            case 2:
                return PaymentType.BOLETO;
            case 3:
                return PaymentType.PIX;
            default:
                System.out.println("Opção inválida. Usando PIX como método padrão.");
                return PaymentType.PIX;
        }
    }

    private static void listAllSales() {
        List<Sale> allSales = salesRepository.findAll();

        if (allSales.isEmpty()) {
            System.out.println("Não há vendas registradas.");
            return;
        }

        System.out.println("\n=== Lista de Vendas ===");
        for (Sale sale : allSales) {
            System.out.println("ID: " + sale.getUuid());
            System.out.println("Cliente: " + sale.getUser().getName());
            System.out.println("Data: " + sale.getSaleDate());
            System.out.println("Método de Pagamento: " + sale.getPaymentMethod());
            System.out.println("Produtos:");

            for (Product product : sale.getProducts()) {
                System.out.printf("  - [ID: %s] %s (R$ %.2f)%n", product.getUuid(), product.getName(), product.getPrice());
            }

            System.out.printf("Valor total: R$ %.2f%n", sale.getTotalAmount());
            System.out.println("---------------------");
        }
    }
}

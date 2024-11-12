package org.example;
import java.math.BigDecimal;
import java.sql.*;

import static org.example.Product.isDifferent;

public class Main {





    public static void main(String[] args) {
            transferDataToDW1();
//        test();



    }
    public static void transferDataToDW() {
        Connection connection = null;
        PreparedStatement insertStmt = null;

        try {
            // Kết nối tới cơ sở dữ liệu MySQL
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/datawarehouse", "root", "");

            // Tắt tự động commit để kiểm soát quá trình commit
            connection.setAutoCommit(false);

            // Câu lệnh INSERT với ON DUPLICATE KEY UPDATE
            String insertQuery = """
    INSERT INTO product_dim (
        product_sk, product_id, name, price, price_sale, brand, color, size, status,
        description_part1, description_part2, description_part3, created_at, last_modified
    )
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
    ON DUPLICATE KEY UPDATE
        price = VALUES(price),
        price_sale = VALUES(price_sale),
        name = VALUES(name),
        color = VALUES(color),
        size = VALUES(size),
        status = VALUES(status),
        description_part1 = VALUES(description_part1),
        description_part2 = VALUES(description_part2),
        description_part3 = VALUES(description_part3),
        last_modified = CURRENT_TIMESTAMP;
""";




            insertStmt = connection.prepareStatement(insertQuery);

            // Lấy dữ liệu từ bảng staging
            String selectQuery = "SELECT naturalId, id, name, price, priceSale, brand, color, size, status, description_part1, description_part2, description_part3 FROM staging.bikes";

            try (Statement selectStmt = connection.createStatement();
                 ResultSet rs = selectStmt.executeQuery(selectQuery)) {

                while (rs.next()) {

                    // Sử dụng parsePrice() để chuẩn hóa giá trị price và priceSale
                    BigDecimal price = parsePrice(rs.getString("price"));
                    BigDecimal priceSale = parsePrice(rs.getString("priceSale"));

                    String color = parseColor(rs.getString("color"));

                    // Set các giá trị cho PreparedStatement
                    insertStmt.setInt(1, rs.getInt("naturalId"));
                    insertStmt.setString(2, rs.getString("id"));
                    insertStmt.setString(3, rs.getString("name"));
                    insertStmt.setBigDecimal(4, price);
                    insertStmt.setBigDecimal(5, priceSale);
                    insertStmt.setString(6, rs.getString("brand"));
                    insertStmt.setString(7, color);  // Giá trị đã chuẩn hóa cho color
                    insertStmt.setString(8, rs.getString("size"));
                    insertStmt.setString(9, rs.getString("status"));
                    insertStmt.setString(10, rs.getString("description_part1"));
                    insertStmt.setString(11, rs.getString("description_part2"));
                    insertStmt.setString(12, rs.getString("description_part3"));

                    insertStmt.addBatch();
                }
            }

            // Thực thi batch insert và thông báo
            int[] affectedRows = insertStmt.executeBatch();

            boolean hasChanges = false;
            for (int row : affectedRows) {
                if (row > 0) {
                    // Kiểm tra và in ra thông báo nếu có thay đổi
                    hasChanges = true;
                    System.out.println("Dữ liệu đã được cập nhật thành công.");
                } else {
                    // Nếu không có thay đổi, kiểm tra trùng lặp
                    System.out.println("Dữ liệu đã trùng lặp và không thay đổi.");
                }
            }

            // Nếu có thay đổi, thực hiện commit
            if (hasChanges) {
                connection.commit();
            }

        } catch (SQLException e) {
            // Rollback nếu có lỗi
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    System.out.println("Lỗi khi rollback: " + rollbackEx.getMessage());
                }
            }
            System.out.println("Lỗi khi kết nối hoặc thực hiện truy vấn: " + e.getMessage());
        } finally {
            // Đóng kết nối và các statement
            try {
                if (insertStmt != null) insertStmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.out.println("Lỗi khi đóng kết nối: " + e.getMessage());
            }
        }
    }

    public static void transferDataToDW1() {
        Connection connection = null;
        PreparedStatement insertStmt = null;
        PreparedStatement updateStmt = null;

        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/datawarehouse", "root", "");

            // Tắt tự động commit để kiểm soát quá trình commit
            connection.setAutoCommit(false);

            // Câu lệnh SELECT để lấy dữ liệu từ bảng staging
            String selectQuery = "SELECT naturalId, id, name, price, priceSale, brand, color, size, status, description_part1, description_part2, description_part3 FROM staging.bikes";

            try (Statement selectStmt = connection.createStatement();
                 ResultSet rs = selectStmt.executeQuery(selectQuery)) {

                // Duyệt qua từng bản ghi trong bảng staging
                while (rs.next()) {
                    // Tạo đối tượng Product mới từ dữ liệu trong bảng staging
                    Product newProduct = new Product();
                    newProduct.setProductSk(rs.getInt("naturalId"));
                    newProduct.setProductId(rs.getString("id"));
                    newProduct.setName(rs.getString("name"));
                    newProduct.setPrice(parsePrice(rs.getString("price")));
                    newProduct.setPriceSale(parsePrice(rs.getString("priceSale")));
                    newProduct.setBrand(rs.getString("brand"));
                    newProduct.setColor(parseColor(rs.getString("color")));
                    newProduct.setSize(rs.getString("size"));
                    newProduct.setStatus(rs.getString("status"));
                    newProduct.setDescriptionPart1(rs.getString("description_part1"));
                    newProduct.setDescriptionPart2(rs.getString("description_part2"));
                    newProduct.setDescriptionPart3(rs.getString("description_part3"));
                    newProduct.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                    newProduct.setLastModified(new Timestamp(System.currentTimeMillis()));

                    // Truy vấn dữ liệu hiện tại từ product_dim để so sánh
                    String selectCurrentQuery = "SELECT * FROM product_dim WHERE product_sk = ?";
                    try (PreparedStatement currentStmt = connection.prepareStatement(selectCurrentQuery)) {
                        currentStmt.setInt(1, newProduct.getProductSk());
                        try (ResultSet currentRs = currentStmt.executeQuery()) {
                            if (currentRs.next()) {
                                // Tạo đối tượng Product cũ
                                Product oldProduct = new Product();
                                oldProduct.setProductSk(currentRs.getInt("product_sk"));
                                oldProduct.setProductId(currentRs.getString("product_id"));
                                oldProduct.setName(currentRs.getString("name"));
                                oldProduct.setPrice(currentRs.getBigDecimal("price"));
                                oldProduct.setPriceSale(currentRs.getBigDecimal("price_sale"));
                                oldProduct.setBrand(currentRs.getString("brand"));
                                oldProduct.setColor(currentRs.getString("color"));
                                oldProduct.setSize(currentRs.getString("size"));
                                oldProduct.setStatus(currentRs.getString("status"));
                                oldProduct.setDescriptionPart1(currentRs.getString("description_part1"));
                                oldProduct.setDescriptionPart2(currentRs.getString("description_part2"));
                                oldProduct.setDescriptionPart3(currentRs.getString("description_part3"));
                                oldProduct.setCreatedAt(currentRs.getTimestamp("created_at"));
                                oldProduct.setLastModified(currentRs.getTimestamp("last_modified"));

                                // So sánh đối tượng cũ và mới
                                if (isDifferent(oldProduct, newProduct)) {
                                    // Nếu có sự thay đổi, thực hiện cập nhật
                                    String updateQuery = """
                                    UPDATE product_dim SET 
                                        price = ?, 
                                        price_sale = ?, 
                                        name = ?, 
                                        color = ?, 
                                        size = ?, 
                                        status = ?, 
                                        description_part1 = ?, 
                                        description_part2 = ?, 
                                        description_part3 = ?, 
                                        last_modified = NOW()
                                    WHERE product_sk = ?;
                                """;
                                    updateStmt = connection.prepareStatement(updateQuery);
                                    updateStmt.setBigDecimal(1, newProduct.getPrice());
                                    updateStmt.setBigDecimal(2, newProduct.getPriceSale());
                                    updateStmt.setString(3, newProduct.getName());
                                    updateStmt.setString(4, newProduct.getColor());
                                    updateStmt.setString(5, newProduct.getSize());
                                    updateStmt.setString(6, newProduct.getStatus());
                                    updateStmt.setString(7, newProduct.getDescriptionPart1());
                                    updateStmt.setString(8, newProduct.getDescriptionPart2());
                                    updateStmt.setString(9, newProduct.getDescriptionPart3());
                                    updateStmt.setInt(10, newProduct.getProductSk());
                                    updateStmt.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }

            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        } finally {
            try {
                if (insertStmt != null) insertStmt.close();
                if (updateStmt != null) updateStmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }



    public static void test() {
        // Thông tin kết nối tới MySQL
        String url = "jdbc:mysql://localhost:3306/dw"; // Địa chỉ cơ sở dữ liệu MySQL
        String username = "root";
        String password = "";  //

        // Kết nối đến cơ sở dữ liệu và thực hiện câu lệnh SELECT
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            if (connection != null) {
                System.out.println("Kết nối đến cơ sở dữ liệu thành công!");

                // Tạo câu lệnh SQL
                String insertQuery = """
    INSERT INTO product_dim (
                             product_sk,
        product_id, name, price, price_sale, brand, color, size, status, 
        description_part1, description_part2, description_part3
    )
    SELECT 
        p.naturalId,p.id, p.name,p.price,
        
       
       
       p.priceSale,

        p.brand, p.color, p.size, p.status, 
        p.description_part1, p.description_part2, p.description_part3
    
    FROM 
                    staging.bikes as p
""";

                PreparedStatement gg = connection.prepareStatement(insertQuery);
                gg.executeUpdate();

            }
        } catch (SQLException e) {
            System.out.println("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
        }
    }
    public static BigDecimal parsePrice(String price) {
        if (price == null || price.isEmpty()) {
            return BigDecimal.ZERO; // hoặc trả về giá trị mặc định
        }
        // Loại bỏ ký tự ₫, dấu chấm và khoảng trắng
        String cleanedPrice = price.replace("₫", "").replace(".", "").replace(" ", "");

        try {
            return new BigDecimal(cleanedPrice);
        } catch (NumberFormatException e) {
            System.out.println("Không thể chuyển đổi giá trị price: " + price);
            return BigDecimal.ZERO; // hoặc trả về giá trị mặc định
        }
    }
    public static String parseColor(String color) {
        if (color == null || color.isEmpty()) {
            return null;
        }

        // Loại bỏ ký tự ":" và các khoảng trắng thừa
        String cleanedColor = color.replace(":", "").replaceAll("\\s{2,}", " ").trim();

        // Trả về chuỗi đã được chuẩn hóa
        return cleanedColor;
    }




}
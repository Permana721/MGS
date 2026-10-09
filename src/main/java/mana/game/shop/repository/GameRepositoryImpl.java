package mana.game.shop.repository;

import mana.game.shop.entity.*;
import mana.game.shop.util.DatabaseUtil;

import javax.sql.DataSource;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.StreamSupport;

import static java.sql.Types.INTEGER;

public class GameRepositoryImpl implements GameRepository {
    private DataSource dataSource;

    public GameRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Game save(Game game) {
        String sql = """
                   INSERT INTO games
                   (title, category, price, type, stock, size_mb, release_date) 
                   VALUES 
                   (?, ?::game_category, ?, ?::game_type, ?, ?, ?)
                   """;

        try(Connection connection = dataSource.getConnection()){
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            statement.setString(1, game.getTitle());
            statement.setString(2, game.getGameCategory().name());
            statement.setBigDecimal(3, game.getPrice());
            statement.setString(4, game.getGameType().name());
            if (Objects.nonNull(game.getStock())) {
                statement.setInt(5, game.getStock());
            } else {
                statement.setNull(5, INTEGER);
            }
            if (Objects.nonNull(game.getSize())) {
                statement.setInt(6, game.getSize());
            } else {
                statement.setNull(6, INTEGER);
            }
            statement.setDate(7, Date.valueOf((LocalDate) game.getRelease_date()));

            statement.executeUpdate();

            try(ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    game.setId(generatedKeys.getInt(1));
                }
            }

            return game;
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public Game update(Game game) {
        String sql = """
                 UPDATE games 
                 SET title = ?, 
                 category = ?::game_category, 
                 price = ?, 
                 game_type = ?::game_type, 
                 stock = ?, 
                 size_mb = ?, 
                 release_date = ?,
                 updated_at = CURRENT_TIMESTAMP
                 WHERE id = ?
                 """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, game.getTitle());
            statement.setString(2, game.getGameCategory().name());
            statement.setBigDecimal(3, game.getPrice());
            statement.setString(4, game.getGameType().name());

            if (Objects.nonNull(game.getStock())) {
                statement.setInt(5, game.getStock());
            } else {
                statement.setNull(5, Types.INTEGER);
            }

            if (Objects.nonNull(game.getSize())) {
                statement.setInt(6, game.getSize());
            } else {
                statement.setNull(6, Types.INTEGER);
            }

            statement.setDate(7, Date.valueOf(game.getRelease_date()));
            statement.setInt(8, game.getId());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Update failed, game with ID " + game.getId() + " not found!");
            }

            return game;

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to update the game: " + exception.getMessage(), exception);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM games WHERE id = ?";
        try(Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, id);
                return preparedStatement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public boolean decreaseStock(Connection connection, int id, int stock) throws SQLException {
        String sql = "UPDATE games SET stock = stock - ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        preparedStatement.setInt(1, stock);
        preparedStatement.setInt(2, id);

        int rowsUpdated = preparedStatement.executeUpdate();
        return rowsUpdated > 0;
    }

    @Override
    public Optional<Game> findById(int id) {
        String sql = "SELECT * FROM games WHERE id = ?";
        try(Connection connection = dataSource.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if (resultSet.next()) {
                    Game game = rowHelper(resultSet);
                    return Optional.of(game);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public List<Game> findByName(String keyword) {
        String sql = "SELECT * FROM games WHERE title ILIKE ?";
        List<Game> games = new ArrayList<>();

        try (Connection connection = DatabaseUtil.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + keyword.trim() + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    games.add(rowHelper(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching games: " + e.getMessage(), e);
        }

        return games;
    }

    private Game rowHelper(ResultSet resultSet) throws SQLException {
        GameType gameType = GameType.valueOf(resultSet.getString("type"));
        GameCategory gameCategory = GameCategory.valueOf(resultSet.getString("category"));
        LocalDate releaseDate = resultSet.getDate("release_date") != null
                ? resultSet.getDate("release_date").toLocalDate()
                : null;

        if (gameType == GameType.DIGITAL) {
            DigitalGame digitalGame = new DigitalGame();
            digitalGame.setId(resultSet.getInt("id"));
            digitalGame.setTitle(resultSet.getString("title"));
            digitalGame.setGameCategory(gameCategory);
            digitalGame.setPrice(resultSet.getBigDecimal("price"));
            digitalGame.setGameType(gameType);
            digitalGame.setSize(resultSet.getInt("size_mb"));
            digitalGame.setRelease_date(releaseDate);
            return digitalGame;
        } else {
            PhysicalGame physicalGame = new PhysicalGame();
            physicalGame.setId(resultSet.getInt("id"));
            physicalGame.setTitle(resultSet.getString("title"));
            physicalGame.setGameCategory(gameCategory);
            physicalGame.setPrice(resultSet.getBigDecimal("price"));
            physicalGame.setGameType(gameType);
            physicalGame.setStock(resultSet.getInt("stock"));
            physicalGame.setRelease_date(releaseDate);
            return physicalGame;
        }
    }

    @Override
    public List<Game> findAll() {
        String sql = "SELECT * FROM games";

        try(Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)) {

            Iterator<Game> iterator = new Iterator<>() {
                @Override
                public boolean hasNext() {
                    try {
                        return resultSet.next();
                    } catch (SQLException exception) {
                        throw new RuntimeException(exception);
                    }
                }

                @Override
                public Game next() {
                    try {
                        return rowHelper(resultSet);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

            return StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                    false
            ).toList();
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }
}

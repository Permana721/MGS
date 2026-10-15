package mana.game.shop.repository;

import mana.game.shop.entity.*;
import mana.game.shop.exception.DuplicateDataException;
import mana.game.shop.exception.ResourceNotFoundException;
import mana.game.shop.util.DatabaseUtil;

import javax.sql.DataSource;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.StreamSupport;

import static java.sql.Types.INTEGER;

public class GameRepositoryImpl implements GameRepository {
    private final DataSource dataSource;

    public GameRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Game> save(Game game) {
        String sql = """
                   INSERT INTO games
                   (title, category, price, type, stock, size_mb, release_date)\s
                   VALUES\s
                   (?, ?::game_category, ?, ?::game_type, ?, ?, ?)
                   RETURNING id, created_at, updated_at
                  \s""";

        try(Connection connection = dataSource.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, game.getTitle());
            preparedStatement.setString(2, game.getGameCategory().name());
            preparedStatement.setBigDecimal(3, game.getPrice());
            preparedStatement.setString(4, game.getGameType().name());

            if (Objects.nonNull(game.getStock())) {
                preparedStatement.setInt(5, game.getStock());
            } else {
                preparedStatement.setNull(5, INTEGER);
            }
            if (Objects.nonNull(game.getSize())) {
                preparedStatement.setInt(6, game.getSize());
            } else {
                preparedStatement.setNull(6, INTEGER);
            }
            preparedStatement.setDate(7, Date.valueOf((LocalDate) game.getRelease_date()));

            preparedStatement.executeUpdate();

            try(ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    game.setId(resultSet.getInt("id"));
                    game.setCreated_at(resultSet.getObject("created_at", OffsetDateTime.class).toInstant());
                    game.setUpdated_at(resultSet.getObject("updated_at", OffsetDateTime.class).toInstant());
                    return Optional.of(rowHelper(resultSet));
                }
            }
            return Optional.empty();
        } catch (SQLException exception) {
            if ("23505".equals(exception.getSQLState())) {
                throw new DuplicateDataException("Game is already exists!");
            }

            throw new RuntimeException("Database error while saving user: " + exception.getMessage(), exception);
        }
    }

    @Override
    public Optional<Game> update(Game game) {
        String sql = """
                 UPDATE games\s
                 SET title = ?,\s
                 category = ?::game_category,\s
                 price = ?,\s
                 game_type = ?::game_type,\s
                 stock = ?,\s
                 size_mb = ?,\s
                 release_date = ?,
                 updated_at = CURRENT_TIMESTAMP
                 WHERE id = ?
                 RETURNING updated_at
                \s""";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, game.getTitle());
            preparedStatement.setString(2, game.getGameCategory().name());
            preparedStatement.setBigDecimal(3, game.getPrice());
            preparedStatement.setString(4, game.getGameType().name());

            if (Objects.nonNull(game.getStock())) {
                preparedStatement.setInt(5, game.getStock());
            } else {
                preparedStatement.setNull(5, Types.INTEGER);
            }

            if (Objects.nonNull(game.getSize())) {
                preparedStatement.setInt(6, game.getSize());
            } else {
                preparedStatement.setNull(6, Types.INTEGER);
            }

            preparedStatement.setDate(7, Date.valueOf(game.getRelease_date()));
            preparedStatement.setInt(8, game.getId());

            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if (resultSet.next()) {
                    game.setUpdated_at(resultSet.getObject("updated_at", OffsetDateTime.class).toInstant());
                    return Optional.of(rowHelper(resultSet));
                }
            }

            return Optional.empty();

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to update the game: " + exception.getMessage(), exception);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = """
                DELETE FROM games\s
                WHERE id = ?
               \s""";
        try(Connection connection = dataSource.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            int result = preparedStatement.executeUpdate();
            return result > 0;
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to delete the game: " + exception.getMessage());
        }
    }

    @Override
    public Optional<Game> decreaseStock(Connection connection, int id, int stock) throws SQLException {
        String sql = """
                UPDATE games\s
                SET stock = stock - ?,\s
                updated_at = CURRENT_TIMESTAMP\s
                WHERE id = ?
                RETURNING updated_at
               \s""";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        preparedStatement.setInt(1, stock);
        preparedStatement.setInt(2, id);

        try(ResultSet resultSet = preparedStatement.executeQuery()){
            if (resultSet.next()) {
                rowHelper(resultSet).setUpdated_at(resultSet.getObject("updated_at", OffsetDateTime.class).toInstant());
                return Optional.of(rowHelper(resultSet));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to decrease stock game: " + exception.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Game> findById(int id) {
        String sql = """
                SELECT * FROM games
                WHERE id = ?
                """;
        try(Connection connection = dataSource.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if (resultSet.next()) {
                    Game game = rowHelper(resultSet);
                    return Optional.of(game);
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to search game: " + exception.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Game> findByName(String title) {
        String sql = "SELECT * FROM games WHERE title ILIKE ? ORDER BY title";
        List<Game> games = new ArrayList<>();

        try (Connection connection = DatabaseUtil.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + title.trim() + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    games.add(rowHelper(resultSet));
                }
            }
            return games;
        } catch (SQLException exception) {
            throw new RuntimeException("Error searching games: " + exception.getMessage());
        }
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
                        throw new ResourceNotFoundException(exception.getMessage());
                    }
                }

                @Override
                public Game next() {
                    try {
                        return rowHelper(resultSet);
                    } catch (SQLException exception) {
                        throw new ResourceNotFoundException(exception.getMessage());
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

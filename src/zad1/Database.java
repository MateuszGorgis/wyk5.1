package zad1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Database {
	private Connection connection;
	private TravelData travelData;
	private String url;
	private Integer id = 1;
	
	public Database(String url, TravelData travelData) {
		this.url = url;
		this.travelData = travelData;
	}

	void create() {
		connect();
		try {
			DatabaseMetaData databaseMetaData;
			if (connection != null) {
				databaseMetaData = connection.getMetaData();
				ResultSet resultSet = databaseMetaData.getTables(null, null, "OFERTA", null);
				if(resultSet.next()) {
					connection.createStatement().executeUpdate("DROP TABLE Oferta");
				}
			}

			connection.createStatement().execute("CREATE TABLE Oferta("
					+ "id int PRIMARY KEY, "
					+ "locale varchar (20), "
					+ "kraj varchar(40), "
					+ "data_wyjazdu Date, "
					+ "data_powrotu Date, "
					+ "miejsce varchar(20), "
					+ "cena varchar(20), "
					+ "symbol_waluty varchar(10))"
					);

			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Oferta VALUES(?,?,?,?,?,?,?,?)");
			travelData.getAllResults().forEach((locale, offerList) -> {
				for (String line : offerList) {
					String[] tokens = line.split("\\t");
					try {
						preparedStatement.setInt(1, id);
					id++;
					preparedStatement.setString(2, locale);
					preparedStatement.setString(3, tokens[0]);
					preparedStatement.setString(4, tokens[1]);
					preparedStatement.setString(5, tokens[2]);
					preparedStatement.setString(6, tokens[3]);
					preparedStatement.setString(7, tokens[4]);
					preparedStatement.setString(8, tokens[5]);
					preparedStatement.executeUpdate();
					} catch (SQLException e) {
						System.err.println("SQL Exception");
						e.printStackTrace();
					}
				}
			});

		} catch (SQLException e) {
			System.err.println("SQL Exception");
			e.printStackTrace();
		}
	}

	void connect(){
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
			connection = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.err.println("Connection exception");
			System.exit(2);
		} catch (ClassNotFoundException e) {
			System.err.println("Driver class not found");
			e.printStackTrace();
			System.exit(2);
		}
	}

	public void showGui() {
		JFrame jFrame = new JFrame();
		jFrame.setTitle("JTable Example");

		Statement statement;
		try {
			statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT * FROM Oferta");
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			int columnCount = resultSetMetaData.getColumnCount();
			List<String> columnNames = new LinkedList<>();
			for (int i = 1; i <= columnCount; i++){
				columnNames.add(resultSetMetaData.getColumnName(i));
			}

			List<List<String>> data = new LinkedList<>();
			while(resultSet.next()) {
				List<String> temporaryData = new LinkedList<>();
				for (int i = 1; i <= columnCount; i++){
					temporaryData.add(resultSet.getString(i));
				}
				data.add(temporaryData);
			}
			String[] stringColumnNames = new String[columnNames.size()];
			final int[] i = {0};
			columnNames.forEach(s -> {
				stringColumnNames[i[0]] = s;
				i[0]++;
			});

			String[][] stringData = new String[data.size()][columnNames.size()];
			final int[] j = {0};
			data.forEach(s -> {
				final int[] k = {0};
				s.forEach(s1 -> {
					stringData[j[0]][k[0]] = s1;
					k[0]++;
				});
				j[0]++;
			});

			ResultSet localeResult = statement.executeQuery("SELECT distinct locale FROM Oferta");
			JComboBox<String> localeFilterList = new JComboBox<>();

			while(localeResult.next()) {
				localeFilterList.addItem(localeResult.getString(1));
			}

			ResultSet countryResult = statement.executeQuery("SELECT distinct kraj FROM Oferta where locale = '" + localeFilterList.getSelectedItem() + "'");
			JComboBox<String> countryFilterList = new JComboBox<>();
			countryFilterList.addItem("");

			while(countryResult.next()) {
				countryFilterList.addItem(countryResult.getString(1));
			}

			JPanel panel = new JPanel();
			panel.add(localeFilterList);
			panel.add(countryFilterList);

			JTable jTable = new JTable(stringData, stringColumnNames);
			jTable.removeColumn(jTable.getColumnModel().getColumn(1));
			jTable.setBounds(30, 40, 200, 300);


			localeFilterList.setSelectedIndex(0);
			TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(jTable.getModel());
			sorter.setRowFilter(RowFilter.regexFilter(localeFilterList.getSelectedItem().toString()));

			JButton button = new JButton("filter");
			panel.add(button);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					List<RowFilter<Object,Object>> filters = new ArrayList<>();
					filters.add(RowFilter.regexFilter(localeFilterList.getSelectedItem().toString()));
					filters.add(RowFilter.regexFilter(countryFilterList.getSelectedItem().toString()));
					RowFilter<Object, Object> rf = RowFilter.andFilter(filters);
					sorter.setRowFilter(rf);
				}
			});
			jTable.setRowSorter(sorter);

			localeFilterList.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					connect();
					try {
						Statement currentStatement = connection.createStatement();
						countryFilterList.removeAllItems();
						ResultSet currentCountryResult = currentStatement.executeQuery("SELECT distinct kraj FROM Oferta where locale = '" + localeFilterList.getSelectedItem() + "'");
						countryFilterList.addItem("");
						while(currentCountryResult.next()) {
							countryFilterList.addItem(currentCountryResult.getString(1));
						}
						currentStatement.close();
						connection.close();
					} catch (SQLException ex) {
						ex.printStackTrace();
					}

				}
			});
			JScrollPane jScrollPane = new JScrollPane(jTable);

			jFrame.add(panel, BorderLayout.SOUTH);
			jFrame.add(jScrollPane);
			jFrame.setSize(500, 200);
			jFrame.setVisible(true);

			statement.close();
			connection.close();
		} catch (SQLException e) {
			System.err.println("SQL exception");
			e.printStackTrace();
			System.exit(2);
		}
	}
}

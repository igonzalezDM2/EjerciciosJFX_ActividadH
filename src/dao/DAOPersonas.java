package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import model.Persona;

public class DAOPersonas {
	
	private enum COLUMNAS {
		ID("id"),
		NOMBRE("nombre"),
		APELLIDOS("apellidos"),
		EDAD("edad");
		private String nombre;
		private COLUMNAS(String nombre) {
			this.nombre = nombre;
		}
		public String getNombre() {
			return nombre;
		}
		
	}
	
	private static final String usuario = "root";
	private static final String contrasena = "root";
	private static final String baseDeDatos = "personas";
	
	/**
	 * NO TE OLVIDES DE CERRAR LA CONEXIÓN, PENDEJO
	 * @return
	 * @throws SQLException
	 */
	public Connection getConexion() throws SQLException {
		return DriverManager.getConnection(String.format("jdbc:mariadb://localhost:3306/%s?user=%s&password=%s", baseDeDatos, usuario, contrasena));
	}
	
	public List<Persona> getPersonas() {
		List<Persona> personas = new LinkedList<>();
		try (Connection con = getConexion()) {
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM Persona");
			iterarResultSet(rs, res -> {
				try {
					String nombre = res.getString(COLUMNAS.NOMBRE.getNombre());
					String apellidos = res.getString(COLUMNAS.APELLIDOS.getNombre());
					int edad = res.getInt(COLUMNAS.EDAD.getNombre());
					personas.add(new Persona(nombre, apellidos, edad));
				} catch (SQLException e) {
					e.printStackTrace();
				}
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return personas;
	}
	
	public void anadirPersona(Persona... persona) throws SQLException {
		if (persona != null) {			
			String sql = "INSERT INTO Persona ('nombre', 'apellidos', 'edad') values (?, ?, ?)";
			try (Connection con = getConexion()) {
				PreparedStatement ps = con.prepareStatement(sql);
				Arrays.stream(persona).forEach(per -> {
					try {
						ps.setString(0, per.getNombre());
						ps.setString(1, per.getApellidos());
						ps.setInt(2, per.getEdad());
						ps.addBatch();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				});
				ps.executeBatch();
			}
		}
	}
	
	public int eliminarPersona(Persona persona) throws SQLException {
		if (persona != null) {
			String sql = "DELETE FROM Persona WHERE nombre = ? and apellidos = ? and edad = ?";
			try (Connection con = getConexion()) {
				PreparedStatement ps = con.prepareStatement(sql);
				ps.setString(0, persona.getNombre());
				ps.setString(1, persona.getApellidos());
				ps.setInt(2, persona.getEdad());
				return ps.executeUpdate();
			}
		}
		return 0;
	}
	
	public Persona updatePersona(Persona persona, String nombre, String apellidos, int edad) throws SQLException {
		String sql = "UPDATE Persona "
				+ String.format("SET nombre = %s, apellidos = %s, edad = %d", nombre, apellidos, edad)
				+ "where nombre = ? and apellidos = ? and edad = ?";
		
		try (Connection con = getConexion()) {
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(0, persona.getNombre());
			ps.setString(1, persona.getApellidos());
			ps.setInt(2, persona.getEdad());
			int afectadas = ps.executeUpdate();
			if (afectadas == 1) {
				persona
				.setNombre(nombre)
				.setApellidos(apellidos)
				.setEdad(edad);
			}
		}
		return persona;
	}
	
	
	//PARA HACER MENOS ENGORROSO ITERAR EL RESULTSET
	@FunctionalInterface
	private interface RSCallback {
		public void hacerCosa(ResultSet rs);
	}
	private void iterarResultSet(ResultSet rs, RSCallback callback) throws SQLException {
		while (rs.next()) {
			callback.hacerCosa(rs);
		}
		rs.close();
	}
	
	
	
}

package flyway.elf;
import fi.nls.oskari.domain.map.view.View;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by PHELESUO on 4.2.2016. Edited by Olli Jakobsson on 11.7.2017
 */
public class V1_0_4__add_coordinatetool_config_supportedprojections implements JdbcMigration  {
    private static final String DEFAULT_LIVI_VIEW = "Default View";
    private static final String DEFAULT_PROJECTION = "EPSG:3067";

    public void migrate(Connection connection) throws Exception {
        /*projection - viewName*/
        HashMap<String, String> names = new HashMap<String, String>();
        names.put(DEFAULT_PROJECTION, DEFAULT_LIVI_VIEW);
        names.put(DEFAULT_PROJECTION_EPSG_3067, DEFAULT_LIVI_VIEW_EPSG_3067);

        JSONObject configJSON = new JSONObject();
        JSONObject supportedProjections = new JSONObject();
        Iterator it = names.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String uuid = getDefaultViewByName(connection, (String)pair.getValue());
            if (uuid != null) {
                supportedProjections.putOnce((String)pair.getKey(), uuid);
            }
        }
        configJSON.putOnce("supportedProjections", supportedProjections);
        String sql = "UPDATE portti_view_bundle_seq set config = '"+configJSON.toString()+"' WHERE bundleinstance = 'coordinatetool'";
        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.execute();
        }
    }

    private String getDefaultViewByName(Connection conn, String viewName) throws SQLException {
        final String sql = "SELECT uuid FROM portti_view where type = 'DEFAULT' AND page='index' and application='servlet' and name ='"+viewName+"'";
        String uuid = null;
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    uuid = rs.getString("uuid");

                    if (rs.next()) {
                        throw new SQLException("More than one rows returned for "+viewName);
                    }
                }
            }
        }
        return uuid;
    }
}

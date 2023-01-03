package net.crashcraft.crashclaim.data.providers.sqlite.versions;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import net.crashcraft.crashclaim.data.providers.sqlite.DataType;
import net.crashcraft.crashclaim.data.providers.sqlite.DataVersion;

import java.sql.SQLException;
import java.util.List;

public class DataRev5 implements DataVersion {
    @Override
    public int getVersion() {
        return 5;
    }

    @Override
    public void executeUpgrade(int fromRevision) throws SQLException {
        DB.executeUpdate("PRAGMA foreign_keys = OFF"); // Turn foreign keys off

        DB.executeUpdate("ALTER TABLE claim_data ADD lowerBoundY INTEGER DEFAULT(30) NOT NULL");

        DB.executeUpdate("PRAGMA foreign_keys = ON");  // Undo
    }
}

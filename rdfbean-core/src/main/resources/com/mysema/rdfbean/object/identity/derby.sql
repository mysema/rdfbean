CREATE TABLE bids(
   lid BIGINT GENERATED ALWAYS AS IDENTITY(START WITH 1, INCREMENT BY 2),
   model INT, 
   id VARCHAR(255)
)

CREATE TABLE uids(
   lid BIGINT GENERATED ALWAYS AS IDENTITY(START WITH 2, INCREMENT BY 2),
   id VARCHAR(255)
)

CREATE TABLE models(
    id INT GENERATED ALWAYS AS IDENTITY,
    model VARCHAR(255)
)

CREATE INDEX bids_lid ON bids(lid)

CREATE INDEX bids_id ON bids(model, id)

CREATE INDEX uids_lid ON uids(lid)

CREATE INDEX uids_id ON uids(id)

CREATE INDEX models_model ON models(model)

CREATE PROCEDURE getlidforbid ( IN model INT, IN id VARCHAR(255))
PARAMETER STYLE JAVA
LANGUAGE JAVA 
MODIFIES SQL DATA
DYNAMIC RESULT SETS 1
EXTERNAL NAME 'com.mysema.rdfbean.object.identity.DerbyProcedures.getLIDForBID'

CREATE PROCEDURE getlidforuid ( IN id VARCHAR(255))
PARAMETER STYLE JAVA
LANGUAGE JAVA 
MODIFIES SQL DATA
DYNAMIC RESULT SETS 1
EXTERNAL NAME 'com.mysema.rdfbean.object.identity.DerbyProcedures.getLIDForUID'

CREATE PROCEDURE getbid ( IN lid BIGINT )
PARAMETER STYLE JAVA
LANGUAGE JAVA 
READS SQL DATA
DYNAMIC RESULT SETS 1
EXTERNAL NAME 'com.mysema.rdfbean.object.identity.DerbyProcedures.getBID'

CREATE PROCEDURE getuid ( IN lid BIGINT )
PARAMETER STYLE JAVA
LANGUAGE JAVA 
READS SQL DATA
DYNAMIC RESULT SETS 1
EXTERNAL NAME 'com.mysema.rdfbean.object.identity.DerbyProcedures.getUID'

CREATE PROCEDURE getmodelid ( IN model VARCHAR(255) )
PARAMETER STYLE JAVA
LANGUAGE JAVA 
MODIFIES SQL DATA
DYNAMIC RESULT SETS 1
EXTERNAL NAME 'com.mysema.rdfbean.object.identity.DerbyProcedures.getModelId'

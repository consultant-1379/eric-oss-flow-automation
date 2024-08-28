--
-- COPYRIGHT Ericsson 2022
--
--
--
-- The copyright to the computer program(s) herein is the property of
--
-- Ericsson Inc. The programs may be used and/or copied only with written
--
-- permission from Ericsson Inc. or in accordance with the terms and
--
-- conditions stipulated in the agreement/contract under which the
--
-- program(s) have been supplied.
--

CREATE OR REPLACE FUNCTION update_fa_schema()
    RETURNS void AS $func$
DECLARE
    -- declare global variables
    relations integer := 5;
    _is_schemaExists INTEGER;
BEGIN
    -- assign value to _is_schemaExists, if this value equals 5 then the flow-automation schema exists
    BEGIN
        SELECT  count(*)
        INTO    _is_schemaExists
        FROM    information_schema.tables
        WHERE   table_schema = 'public'
                AND table_name IN ( 'fa_flow', 'fa_flow_detail','fa_flow_execution', 'fa_flow_execution_report_variable', 'fa_flow_execution_event' );
    END;
    -- if the flow-automation schema exists then check for updated fa_flow_detail relation
    IF _is_schemaExists = relations THEN
        -- check for existence of is_report_supported attribute in fa_flow_detail relation
        IF NOT EXISTS ( SELECT  1
                        FROM    information_schema.columns
                        WHERE   table_schema = 'public'
                                AND table_name = 'fa_flow_detail'
                                AND column_name = 'is_report_supported' ) THEN
            -- fa_flow_detail relation has not been updated as the is_report_supported attribute does not exist, add is_report_supported attribute and update values for is_report_supported
            BEGIN
                ALTER TABLE fa_flow_detail
                ADD is_report_supported boolean default false;
            END;
            BEGIN
                UPDATE  fa_flow_detail
                SET     is_report_supported = true
                WHERE   deployment_id IN ( SELECT   DEPLOYMENT_ID_
                                           FROM     ACT_GE_BYTEARRAY
                                           WHERE    NAME_ = 'report/flow-report-schema.json' );
            END;
        END IF;
    END IF;
END;
$func$ LANGUAGE plpgsql;

-- execute function
SELECT update_fa_schema();

-- drop function
DROP FUNCTION update_fa_schema();
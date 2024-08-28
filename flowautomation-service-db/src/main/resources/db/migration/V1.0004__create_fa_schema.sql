--
-- COPYRIGHT Ericsson 2021
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

CREATE OR REPLACE FUNCTION create_flowautomation_schema()
  RETURNS void AS $func$
DECLARE
    -- Global variables
    tables integer := 5;
    _is_schemaExists INTEGER;
BEGIN
    SELECT count(*) INTO _is_schemaexists FROM information_schema.tables WHERE table_schema='public' AND table_name IN ('fa_flow', 'fa_flow_detail','fa_flow_execution', 'fa_flow_execution_report_variable', 'fa_flow_execution_event');

    IF _is_schemaExists <> tables THEN

        -- 1. Table to store flows.
        CREATE TABLE fa_flow (
            id                                 BIGSERIAL NOT NULL,
            flow_id                            varchar(255) NOT NULL,
            name                               varchar(255) NOT NULL,
            status                             varchar(64) NOT NULL,
            source                             varchar(64)NOT NULL,
            CONSTRAINT fa_flow_primary_key_constraint PRIMARY KEY(id),
            CONSTRAINT fa_flow_name_unique_constraint UNIQUE(name),
            CONSTRAINT fa_flow_flow_id_unique_constraint UNIQUE(flow_id)
        )
        WITH (
            OIDS=FALSE
        );
        CREATE INDEX fa_flow_flow_id_index ON fa_flow USING BTREE (flow_id);

        -- 2. Table to store flow details.
        CREATE TABLE fa_flow_detail (
            id                                  BIGSERIAL NOT NULL,
            fa_flow_id                          BIGSERIAL NOT NULL,
            process_definition_key              varchar(255) NOT NULL,
            version                             varchar(64) NOT NULL,
            description                         text NOT NULL,
            setup_id                            varchar(255),
            execute_id                          varchar(255) NOT NULL,
            imported_by_user                    varchar(64) NOT NULL,
            imported_date                       timestamp with time zone NOT NULL,
            is_active                           bool NOT NULL,
            deployment_id                       varchar(64) NOT NULL,
            back_enabled                        boolean default false,
            CONSTRAINT fa_flow_detail_primary_key_constraint PRIMARY KEY(id),
            CONSTRAINT fa_flow_detail_process_definition_key_unique_constraint UNIQUE(process_definition_key),
            CONSTRAINT Ref_fa_flow_detail_to_fa_flow FOREIGN KEY (fa_flow_id) REFERENCES fa_flow(id) MATCH SIMPLE ON DELETE CASCADE ON UPDATE CASCADE NOT DEFERRABLE
        )
        WITH (
            OIDS=FALSE
        );
        CREATE INDEX fa_flow_detail_process_definition_key_index ON fa_flow_detail USING BTREE (process_definition_key);
        CREATE INDEX fa_flow_detail_flow_id_index ON fa_flow_detail USING BTREE (fa_flow_id);

        -- 3. Table to store flow execution.
        CREATE TABLE fa_flow_execution (
            id                                   BIGSERIAL NOT NULL,
            fa_flow_detail_id                    BIGSERIAL NOT NULL,
            process_instance_id                  varchar(64) NOT NULL,
            flow_execution_name                  varchar(255) NOT NULL,
            executed_by_user                     varchar(64) NOT NULL,
            process_instance_business_key        varchar(255),
            CONSTRAINT fa_flow_execution_primary_key_constraint PRIMARY KEY(id),
            CONSTRAINT fa_flow_execution_process_instance_id_unique_constraint UNIQUE(process_instance_id),
            CONSTRAINT Ref_fa_flow_execution_to_fa_flow_detail FOREIGN KEY (fa_flow_detail_id) REFERENCES fa_flow_detail(id) MATCH SIMPLE ON DELETE CASCADE ON UPDATE CASCADE NOT DEFERRABLE
        )
        WITH (
            OIDS=FALSE
        );
        CREATE INDEX fa_flow_execution_fa_flow_detail_id_index ON fa_flow_execution USING BTREE (fa_flow_detail_id);
        CREATE INDEX fa_flow_execution_process_instance_id_index ON fa_flow_execution USING BTREE (process_instance_id);

        -- 4. Table to store report variables.
        CREATE TABLE fa_flow_execution_report_variable (
            id                                  BIGSERIAL NOT NULL,
            fa_flow_execution_id                BIGSERIAL NOT NULL,
            name                                varchar(255) NOT NULL,
            value                               text,
            size                                integer,
            created_time                        timestamp with time zone NOT NULL default NOW(),
            CONSTRAINT fa_flow_execution_report_variable_primary_key_constraint PRIMARY KEY(id),
            CONSTRAINT Ref_fa_flow_execution_report_variable_to_fa_flow_execution FOREIGN KEY (fa_flow_execution_id) REFERENCES fa_flow_execution(id) MATCH SIMPLE ON DELETE CASCADE ON UPDATE CASCADE NOT DEFERRABLE
        )
        WITH (
            OIDS=FALSE
        );
        CREATE INDEX fa_flow_execution_report_variable_fa_flow_execution_id_index ON fa_flow_execution_report_variable USING BTREE (fa_flow_execution_id);

        -- 5. Table to store flow execution events recorded by the flow.
        CREATE TABLE fa_flow_execution_event (
            id                                   BIGSERIAL NOT NULL,
            event_time                           timestamp with time zone NOT NULL default NOW(),
            event_severity                       varchar(16) NOT NULL,
            target                               varchar(255),
            message                              text,
            event_data                           text,
            fa_flow_execution_id                 BIGSERIAL NOT NULL,
            CONSTRAINT fa_flow_execution_event_primary_key_constraint PRIMARY KEY(id),
            CONSTRAINT Ref_fa_flow_execution_event_to_fa_flow_execution FOREIGN KEY (fa_flow_execution_id) REFERENCES fa_flow_execution(id) MATCH SIMPLE ON DELETE CASCADE ON UPDATE
            CASCADE NOT DEFERRABLE
        )
        WITH (
            OIDS=FALSE
        );
        CREATE INDEX fa_flow_execution_event_target_event_severity ON fa_flow_execution_event USING BTREE (target,event_severity);
        CREATE INDEX fa_flow_execution_event_event_severity_target ON fa_flow_execution_event USING BTREE (event_severity,target);
     END IF;
END
$func$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION create_administrator_user_for_Camunda()
    RETURNS void AS $func$
DECLARE

BEGIN

DO $$
BEGIN
  IF NOT EXISTS (SELECT * FROM act_id_user WHERE id_ = 'administrator') THEN
    INSERT INTO public.act_id_user (id_, rev_, first_, last_, email_, pwd_, salt_, lock_exp_time_, attempts_, picture_id_) VALUES ('administrator', 1, 'administrator', 'administrator', '', '{SHA-512}X/xKo/w2RIT+FZGJmF23JztEjmlM2ctf3kYt1JhTCX8HG69oQy2q4Q7la0A9feVvim/tXsvNAPCqa1At8e9Jeg==', 'HPxekiJtn/tZKtWTJNnCkg==', null, null, null);

    INSERT INTO public.act_id_group (id_, rev_, name_, type_) VALUES ('camunda-admin', 1, 'camunda BPM Administrators', 'SYSTEM');

    INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('administrator', 'camunda-admin');
  END IF;
END
$$;

END
$func$ LANGUAGE plpgsql;

--Execute functions.
SELECT create_flowautomation_schema();
SELECT create_administrator_user_for_Camunda();

--Drop functions.
DROP FUNCTION create_flowautomation_schema();
DROP FUNCTION create_administrator_user_for_Camunda();

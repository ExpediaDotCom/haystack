USE `aa_model_service`;

DROP PROCEDURE IF EXISTS insert_mapping;
DROP PROCEDURE IF EXISTS insert_model;
DROP PROCEDURE IF EXISTS insert_detector;

DROP PROCEDURE IF EXISTS insert_mapping_wildcard_metric_targets_to_model;
DELIMITER //

CREATE PROCEDURE insert_detector (
  IN uuid CHAR(36),
  IN type_ukey VARCHAR(100)
)
  BEGIN
    DECLARE type_id INT(5) UNSIGNED;

    SELECT t.id INTO type_id FROM model_type t WHERE t.ukey = type_ukey;
    INSERT INTO detector (uuid, model_type_id) VALUES (uuid, type_id);
  END //

CREATE PROCEDURE insert_model (
  IN uuid CHAR(36),
  IN params json,
  IN date_created timestamp
)
  BEGIN
    DECLARE detector_id INT(5) UNSIGNED;

    SELECT d.id INTO detector_id FROM detector d WHERE d.uuid = uuid;
    INSERT INTO model (detector_id, params , date_created) VALUES (detector_id, params, date_created);
  END //

CREATE PROCEDURE insert_mapping (
  IN metric_hash CHAR(36),
  IN detector_uuid CHAR(36)
)
  BEGIN
    DECLARE metric_id INT(10) UNSIGNED;
    DECLARE detector_id INT(10) UNSIGNED;

    SELECT m.id INTO metric_id FROM metric m WHERE m.hash = metric_hash;
    SELECT m.id INTO detector_id FROM detector m WHERE m.uuid = detector_uuid;
    INSERT INTO metric_detector_mapping (metric_id, detector_id) VALUES (metric_id, detector_id);
  END //

CREATE PROCEDURE insert_mapping_wildcard_metric_targets_to_detector (
  IN detector_uuid CHAR(36),
  IN metric_ukey CHAR(100)
)
  BEGIN
    DECLARE metric_id INT(10) UNSIGNED;
    DECLARE detector_id INT(10) UNSIGNED;
    DECLARE done INT DEFAULT 0;
    DECLARE present INT DEFAULT 0;
    DECLARE cur1 cursor for SELECT m.id FROM metric m WHERE m.ukey LIKE metric_ukey;
    DECLARE continue handler for not found set done=1;

    open cur1;

    REPEAT
      FETCH cur1 into metric_id;
      if NOT done then
        SELECT id INTO detector_id FROM detector WHERE uuid = detector_uuid;
        IF NOT EXISTS (SELECT 1 FROM metric_detector_mapping m3 WHERE m3.metric_id = metric_id and m3.detector_id = detector_id)
        THEN
          INSERT INTO metric_detector_mapping (metric_id, detector_id) VALUES (metric_id, detector_id);
        END IF;
      END IF;
    UNTIL done END REPEAT;
    close cur1;
  END //

DELIMITER ;

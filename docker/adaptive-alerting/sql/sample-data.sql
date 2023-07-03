USE `aa_model_service`;

INSERT INTO `metric` (`ukey`, `hash`, `tags`) VALUES
  ('karmalab.stats.gauges.AirBoss.chelappabo003_karmalab_net.java.lang.Threading.ThreadCount', '1.71828d68a2938ff1ef96c340f12e2dd6', '{"unit": "unknown", "mtype": "gauge", "org_id": "1", "interval": "30"}')
;
INSERT INTO `metric` (`ukey`, `hash`, `tags`) VALUES
  ('dummy.metric', '1.25345234523452352253452f12e2dd6', '{"unit": "unknown", "mtype": "gauge", "org_id": "1", "interval": "30"}')
;


INSERT INTO `model_type` (`ukey`) VALUES
  ('constant-detector'),
  ('cusum-detector'),
  ('ewma-detector'),
  ('individuals-detector'),
  ('pewma-detector'),
  ('rcf-detector')
;

CALL insert_detector('3ec81aa2-2cdc-415e-b4f3-c1beb223ae60','cusum-detector');
CALL insert_detector('2cdc1aa2-2cdc-355e-b4f3-d2beb223ae60','constant-detector');

CALL insert_model('3ec81aa2-2cdc-415e-b4f3-c1beb223ae60','{"alpha":40,"beta":30}', '2018-10-10 10:02:04');
CALL insert_model('3ec81aa2-2cdc-415e-b4f3-c1beb223ae60','{"alpha":100,"beta":455}','2018-10-12 17:01:04');
CALL insert_model('2cdc1aa2-2cdc-355e-b4f3-d2beb223ae60','{"low":100,"high":455}','2018-10-10 10:01:04');
CALL insert_model('2cdc1aa2-2cdc-355e-b4f3-d2beb223ae60','{"low":100,"high":455}', '2018-10-12 17:01:04');

CALL insert_mapping('1.71828d68a2938ff1ef96c340f12e2dd6', '3ec81aa2-2cdc-415e-b4f3-c1beb223ae60');
CALL insert_mapping('1.25345234523452352253452f12e2dd6', '2cdc1aa2-2cdc-355e-b4f3-d2beb223ae60');

INSERT INTO sensors (id, name, type) VALUES ('e3242ea2-0514-46d3-aad8-b2012980c41c', 'Temperature Sensor 1',  'TEMPERATURE');
INSERT INTO sensors (id, name, type) VALUES ('ac723c77-955f-469d-9d6a-d56bac39c202', 'Humidity Sensor 1',  'HUMIDITY');

INSERT INTO sensor_readings(id, value, timestamp, sensor_id) VALUES (1, 25.3, '2020-06-22 19:10:25', 'e3242ea2-0514-46d3-aad8-b2012980c41c');
INSERT INTO sensor_readings(id, value, timestamp, sensor_id) VALUES (2, 24.2, '2020-06-23 21:45:56', 'e3242ea2-0514-46d3-aad8-b2012980c41c');
INSERT INTO sensor_readings(id, value, timestamp, sensor_id) VALUES (3, 28.4, '2020-06-24 18:23:12', 'e3242ea2-0514-46d3-aad8-b2012980c41c');

INSERT INTO sensor_readings(id, value, timestamp, sensor_id) VALUES (4, 0.45, '2020-06-22 16:22:22', 'ac723c77-955f-469d-9d6a-d56bac39c202');
INSERT INTO sensor_readings(id, value, timestamp, sensor_id) VALUES (5, 0.48, '2020-06-23 18:56:43', 'ac723c77-955f-469d-9d6a-d56bac39c202');

INSERT INTO alerts (id, message, timestamp, sensor_id) VALUES (1, 'Temperature alert',  '2020-06-24 18:23:12', 'e3242ea2-0514-46d3-aad8-b2012980c41c');

SELECT setval('sensor_readings_seq', (SELECT MAX(id) FROM sensor_readings));
SELECT setval('alerts_seq', (SELECT MAX(id) FROM alerts));

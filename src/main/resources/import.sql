-- =====================================================
-- SEED DATA: Doctors, Availability Slots
-- Dates are relative to CURRENT_DATE so slots are always
-- valid regardless of when the app starts.
-- =====================================================

-- ==================== DOCTORS ====================
INSERT INTO doctor(id, name, specialty) VALUES (1, 'Dr. Sharma', 'Orthopedics');
INSERT INTO doctor(id, name, specialty) VALUES (2, 'Dr. Patel', 'Orthopedics');
INSERT INTO doctor(id, name, specialty) VALUES (3, 'Dr. Rao', 'General Medicine');
INSERT INTO doctor(id, name, specialty) VALUES (4, 'Dr. Gupta', 'Cardiology');
INSERT INTO doctor(id, name, specialty) VALUES (5, 'Dr. Iyer', 'General Medicine');
INSERT INTO doctor(id, name, specialty) VALUES (6, 'Dr. Nair', 'Neurology');

-- ==================== AVAILABILITY SLOTS ====================
-- Dr. Sharma (Orthopedics) — today, +1, +2, +4
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (1,  1, DATEADD('DAY', 0, CURRENT_DATE), '09:00', '09:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (2,  1, DATEADD('DAY', 0, CURRENT_DATE), '09:30', '10:00', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (3,  1, DATEADD('DAY', 0, CURRENT_DATE), '10:00', '10:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (4,  1, DATEADD('DAY', 0, CURRENT_DATE), '10:30', '11:00', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (5,  1, DATEADD('DAY', 0, CURRENT_DATE), '14:00', '14:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (6,  1, DATEADD('DAY', 0, CURRENT_DATE), '14:30', '15:00', false);

INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (7,  1, DATEADD('DAY', 1, CURRENT_DATE), '09:00', '09:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (8,  1, DATEADD('DAY', 1, CURRENT_DATE), '09:30', '10:00', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (9,  1, DATEADD('DAY', 1, CURRENT_DATE), '10:00', '10:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (10, 1, DATEADD('DAY', 1, CURRENT_DATE), '10:30', '11:00', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (11, 1, DATEADD('DAY', 1, CURRENT_DATE), '14:00', '14:30', false);

INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (12, 1, DATEADD('DAY', 2, CURRENT_DATE), '09:00', '09:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (13, 1, DATEADD('DAY', 2, CURRENT_DATE), '10:30', '11:00', false);

INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (14, 1, DATEADD('DAY', 4, CURRENT_DATE), '09:00', '09:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (15, 1, DATEADD('DAY', 4, CURRENT_DATE), '14:00', '14:30', false);

-- Dr. Patel (Orthopedics) — today, +1, +2, +3
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (16, 2, DATEADD('DAY', 0, CURRENT_DATE), '11:00', '11:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (17, 2, DATEADD('DAY', 0, CURRENT_DATE), '11:30', '12:00', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (18, 2, DATEADD('DAY', 0, CURRENT_DATE), '15:00', '15:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (19, 2, DATEADD('DAY', 0, CURRENT_DATE), '15:30', '16:00', false);

INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (20, 2, DATEADD('DAY', 1, CURRENT_DATE), '09:00', '09:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (21, 2, DATEADD('DAY', 1, CURRENT_DATE), '09:30', '10:00', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (22, 2, DATEADD('DAY', 1, CURRENT_DATE), '14:00', '14:30', false);

INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (23, 2, DATEADD('DAY', 2, CURRENT_DATE), '10:00', '10:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (24, 2, DATEADD('DAY', 2, CURRENT_DATE), '10:30', '11:00', false);

INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (25, 2, DATEADD('DAY', 3, CURRENT_DATE), '09:00', '09:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (26, 2, DATEADD('DAY', 3, CURRENT_DATE), '14:00', '14:30', false);

-- Dr. Rao (General Medicine) — today, +1, +3, +5
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (27, 3, DATEADD('DAY', 0, CURRENT_DATE), '09:00', '09:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (28, 3, DATEADD('DAY', 0, CURRENT_DATE), '09:30', '10:00', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (29, 3, DATEADD('DAY', 0, CURRENT_DATE), '10:00', '10:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (30, 3, DATEADD('DAY', 0, CURRENT_DATE), '14:00', '14:30', false);

INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (31, 3, DATEADD('DAY', 1, CURRENT_DATE), '09:00', '09:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (32, 3, DATEADD('DAY', 1, CURRENT_DATE), '11:00', '11:30', false);

INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (33, 3, DATEADD('DAY', 3, CURRENT_DATE), '10:00', '10:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (34, 3, DATEADD('DAY', 3, CURRENT_DATE), '14:00', '14:30', false);

INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (35, 3, DATEADD('DAY', 5, CURRENT_DATE), '09:00', '09:30', false);

-- Dr. Gupta (Cardiology) — today, +1, +3, +6
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (36, 4, DATEADD('DAY', 0, CURRENT_DATE), '10:00', '10:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (37, 4, DATEADD('DAY', 0, CURRENT_DATE), '10:30', '11:00', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (38, 4, DATEADD('DAY', 0, CURRENT_DATE), '15:00', '15:30', false);

INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (39, 4, DATEADD('DAY', 1, CURRENT_DATE), '09:00', '09:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (40, 4, DATEADD('DAY', 1, CURRENT_DATE), '09:30', '10:00', false);

INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (41, 4, DATEADD('DAY', 3, CURRENT_DATE), '10:00', '10:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (42, 4, DATEADD('DAY', 3, CURRENT_DATE), '14:00', '14:30', false);

INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (43, 4, DATEADD('DAY', 6, CURRENT_DATE), '09:00', '09:30', false);

-- Dr. Iyer (General Medicine) — today, +1, +2, +5
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (44, 5, DATEADD('DAY', 0, CURRENT_DATE), '11:00', '11:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (45, 5, DATEADD('DAY', 0, CURRENT_DATE), '11:30', '12:00', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (46, 5, DATEADD('DAY', 0, CURRENT_DATE), '14:00', '14:30', false);

INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (47, 5, DATEADD('DAY', 1, CURRENT_DATE), '10:00', '10:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (48, 5, DATEADD('DAY', 1, CURRENT_DATE), '10:30', '11:00', false);

INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (49, 5, DATEADD('DAY', 2, CURRENT_DATE), '09:00', '09:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (50, 5, DATEADD('DAY', 2, CURRENT_DATE), '14:00', '14:30', false);

INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (51, 5, DATEADD('DAY', 5, CURRENT_DATE), '10:00', '10:30', false);

-- Dr. Nair (Neurology) — today, +1, +4, +6
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (52, 6, DATEADD('DAY', 0, CURRENT_DATE), '09:00', '09:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (53, 6, DATEADD('DAY', 0, CURRENT_DATE), '09:30', '10:00', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (54, 6, DATEADD('DAY', 0, CURRENT_DATE), '14:00', '14:30', false);

INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (55, 6, DATEADD('DAY', 1, CURRENT_DATE), '11:00', '11:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (56, 6, DATEADD('DAY', 1, CURRENT_DATE), '14:00', '14:30', false);

INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (57, 6, DATEADD('DAY', 4, CURRENT_DATE), '09:00', '09:30', false);
INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (58, 6, DATEADD('DAY', 4, CURRENT_DATE), '10:00', '10:30', false);

INSERT INTO availability_slot(id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES (59, 6, DATEADD('DAY', 6, CURRENT_DATE), '09:00', '09:30', false);

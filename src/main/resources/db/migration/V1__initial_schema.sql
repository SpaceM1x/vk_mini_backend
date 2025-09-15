-- V1__initial_schema.sql

-- Create cls_course_category table
CREATE TABLE public.cls_course_category (
                                            id BIGINT NOT NULL AUTO_INCREMENT,
                                            name VARCHAR(255),
                                            description TEXT,
                                            PRIMARY KEY (id)
);

-- Create cls_course_status table
CREATE TABLE public.cls_course_status (
                                          id BIGINT NOT NULL AUTO_INCREMENT,
                                          name VARCHAR(255),
                                          PRIMARY KEY (id)
);

-- Create cls_activation_status table
CREATE TABLE public.cls_activation_status (
                                              id BIGINT NOT NULL AUTO_INCREMENT,
                                              name VARCHAR(255) NOT NULL,
                                              code VARCHAR(255) NOT NULL,
                                              is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                                              PRIMARY KEY (id),
                                              CONSTRAINT uk_cls_activation_status_code UNIQUE (code)
);

-- Create cls_user table
CREATE TABLE public.cls_user (
                                 id BIGINT NOT NULL AUTO_INCREMENT,
                                 login VARCHAR(255) NOT NULL,
                                 password VARCHAR(255) NOT NULL,
                                 id_status BIGINT,
                                 is_deleted BOOLEAN NOT NULL,
                                 name VARCHAR(255) NOT NULL,
                                 PRIMARY KEY (id),
                                 FOREIGN KEY (id_status) REFERENCES public.cls_activation_status(id)
);

-- Create reg_user_token table
CREATE TABLE public.reg_user_token (
                                       id BIGINT NOT NULL AUTO_INCREMENT,
                                       id_user BIGINT NOT NULL,
                                       token VARCHAR(255) NOT NULL,
                                       expiry_date TIMESTAMP NOT NULL,
                                       PRIMARY KEY (id),
                                       FOREIGN KEY (id_user) REFERENCES public.cls_user(id),
                                       CONSTRAINT uk_reg_user_token_token UNIQUE (token)
);

-- Create reg_course table
CREATE TABLE public.reg_course (
                                   id BIGINT NOT NULL AUTO_INCREMENT,
                                   title VARCHAR(255),
                                   description TEXT,
                                   avatar_url VARCHAR(255),
                                   id_course_category BIGINT,
                                   PRIMARY KEY (id),
                                   FOREIGN KEY (id_course_category) REFERENCES public.cls_course_category(id)
);

-- Create cls_lesson table
CREATE TABLE public.cls_lesson (
                                   id BIGINT NOT NULL AUTO_INCREMENT,
                                   id_course BIGINT,
                                   title VARCHAR(255),
                                   video_url VARCHAR(255),
                                   lecture_text TEXT,
                                   lesson_order BIGINT NOT NULL,
                                   PRIMARY KEY (id),
                                   FOREIGN KEY (id_course) REFERENCES public.reg_course(id)
);

-- Create cls_quiz table
CREATE TABLE public.cls_quiz (
                                 id BIGINT NOT NULL AUTO_INCREMENT,
                                 id_lesson BIGINT,
                                 title VARCHAR(255),
                                 description TEXT,
                                 passing_score INTEGER DEFAULT 70,
                                 PRIMARY KEY (id),
                                 FOREIGN KEY (id_lesson) REFERENCES public.cls_lesson(id)
);

-- Create reg_quiz_question table
CREATE TABLE public.reg_quiz_question (
                                          id BIGINT NOT NULL AUTO_INCREMENT,
                                          id_quiz BIGINT,
                                          question_text TEXT,
                                          points INTEGER DEFAULT 1,
                                          question_order INTEGER NOT NULL,
                                          PRIMARY KEY (id),
                                          FOREIGN KEY (id_quiz) REFERENCES public.cls_quiz(id)
);

-- Create reg_quiz_question_option table
CREATE TABLE public.reg_quiz_question_option (
                                                 id BIGINT NOT NULL AUTO_INCREMENT,
                                                 id_quiz_question BIGINT,
                                                 option_text TEXT,
                                                 is_correct BOOLEAN NOT NULL DEFAULT FALSE,
                                                 option_order INTEGER NOT NULL,
                                                 PRIMARY KEY (id),
                                                 FOREIGN KEY (id_quiz_question) REFERENCES public.reg_quiz_question(id)
);

-- Create reg_user_course table
CREATE TABLE public.reg_user_course (
                                        id BIGINT NOT NULL AUTO_INCREMENT,
                                        id_user BIGINT,
                                        id_course BIGINT,
                                        id_course_status BIGINT,
                                        enrolled TIMESTAMP,
                                        completed TIMESTAMP,
                                        PRIMARY KEY (id),
                                        FOREIGN KEY (id_user) REFERENCES public.cls_user(id),
                                        FOREIGN KEY (id_course) REFERENCES public.reg_course(id),
                                        FOREIGN KEY (id_course_status) REFERENCES public.cls_course_status(id)
);

-- Create reg_user_lesson_progress table
CREATE TABLE public.reg_user_lesson_progress (
                                                 id BIGINT NOT NULL AUTO_INCREMENT,
                                                 id_user BIGINT,
                                                 id_lesson BIGINT,
                                                 video_completed BOOLEAN,
                                                 lecture_completed BOOLEAN,
                                                 quiz_completed BOOLEAN,
                                                 PRIMARY KEY (id),
                                                 FOREIGN KEY (id_user) REFERENCES public.cls_user(id),
                                                 FOREIGN KEY (id_lesson) REFERENCES public.cls_lesson(id)
);
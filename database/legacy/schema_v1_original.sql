CREATE TABLE IF NOT EXISTS paper (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(512) NOT NULL,
  authors VARCHAR(512) NOT NULL,
  venue VARCHAR(255),
  abstract TEXT,
  upload_time DATETIME,
  citations INT DEFAULT 0,
  impact VARCHAR(64),
  status VARCHAR(32),
  paper_url VARCHAR(512),
  code_url VARCHAR(512),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS dataset (
  dataset_id INT PRIMARY KEY AUTO_INCREMENT,
  dataset_name VARCHAR(255) NOT NULL,
  description TEXT,
  status_id INT NOT NULL,
  size DOUBLE,
  size_formatted VARCHAR(255),
  file_count INT,
  tags VARCHAR(255),
  user_id INT NOT NULL,
  downloadCount INT,
  created_time DATETIME,
  updated_time DATETIME,
  base_address VARCHAR(255),
  version INT,
  is_deleted INT NOT NULL
);

CREATE TABLE IF NOT EXISTS repository (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  paper_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  url VARCHAR(512) NOT NULL,
  description VARCHAR(512),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (paper_id) REFERENCES paper(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS paper_task (
  paper_id BIGINT NOT NULL,
  task_id BIGINT NOT NULL,
  PRIMARY KEY (paper_id, task_id),
  FOREIGN KEY (paper_id) REFERENCES paper(id) ON DELETE CASCADE,
  FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS paper_dataset (
  paper_id BIGINT NOT NULL,
  dataset_id INT NOT NULL,
  PRIMARY KEY (paper_id, dataset_id),
  FOREIGN KEY (paper_id) REFERENCES paper(id) ON DELETE CASCADE,
  FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS result (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  paper_id BIGINT NOT NULL,
  task_id BIGINT NOT NULL,
  dataset_id INT NOT NULL,
  model_name VARCHAR(255),
  metric_name VARCHAR(128) NOT NULL,
  metric_value VARCHAR(64) NOT NULL,
  is_sota TINYINT(1) DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (paper_id) REFERENCES paper(id) ON DELETE CASCADE,
  FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE,
  FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id) ON DELETE CASCADE,
  INDEX idx_result_paper (paper_id),
  INDEX idx_result_task (task_id),
  INDEX idx_result_dataset (dataset_id)
);

CREATE TABLE IF NOT EXISTS folder (
  folder_id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  parent_id INT NULL,
  user_id INT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT(1) DEFAULT 0,
  FOREIGN KEY (parent_id) REFERENCES folder(folder_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS folder_paper (
  folder_id INT NOT NULL,
  paper_id BIGINT NOT NULL,
  PRIMARY KEY (folder_id, paper_id),
  FOREIGN KEY (folder_id) REFERENCES folder(folder_id) ON DELETE CASCADE,
  FOREIGN KEY (paper_id) REFERENCES paper(id) ON DELETE CASCADE
);

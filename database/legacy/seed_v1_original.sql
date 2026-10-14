INSERT INTO paper (title, authors, venue, abstract, upload_time, citations, impact, status, paper_url, code_url)
VALUES
('Deep Learning for Computer Vision: A Comprehensive Survey','张三, 李四, 王五','IEEE TPAMI','综述', '2024-01-15 00:00:00',156,'IF:17.73','已阅读','https://arxiv.org/abs/1904.05049','https://github.com/paperswithcode'),
('Attention Mechanisms in Natural Language Processing: Recent Advances','李四, 赵六, 钱七','ACL 2024','注意力机制综述','2024-01-10 00:00:00',89,'顶级会议','正在阅读','https://arxiv.org/abs/1706.03762','https://github.com/tensorflow/tensor2tensor');

INSERT INTO task (name) VALUES
('图像分类'),('目标检测'),('机器翻译'),('文本摘要'),('语言建模');

INSERT INTO dataset (dataset_id, dataset_name, description, status_id, size, size_formatted, file_count, tags, user_id, downloadCount, created_time, updated_time, base_address, version, is_deleted)
VALUES
(1,'ImageNet','大规模图像分类数据集',1,150.0,'150GB',14197122,'图像分类,benchmark',1,100000,'2024-01-01 00:00:00','2024-01-01 00:00:00','https://image-net.org',1,0),
(2,'COCO','目标检测与分割数据集',1,25.0,'25GB',330000,'目标检测,分割',1,80000,'2024-01-01 00:00:00','2024-01-01 00:00:00','https://cocodataset.org',1,0),
(3,'WMT14','机器翻译英法数据集',1,5.0,'5GB',5000000,'机器翻译,NLP',1,5000,'2024-01-01 00:00:00','2024-01-01 00:00:00','https://www.statmt.org/wmt14',1,0),
(4,'WikiText','语言建模数据集',1,1.2,'1.2GB',103000,'语言建模,NLP',1,6000,'2024-01-01 00:00:00','2024-01-01 00:00:00','https://blog.einstein.ai/the-wikitext-long-term-dependency-language-modeling-dataset/',1,0)
ON DUPLICATE KEY UPDATE 
dataset_name=VALUES(dataset_name),
description=VALUES(description),
status_id=VALUES(status_id),
size=VALUES(size),
size_formatted=VALUES(size_formatted),
file_count=VALUES(file_count),
tags=VALUES(tags),
user_id=VALUES(user_id),
downloadCount=VALUES(downloadCount),
updated_time=VALUES(updated_time),
base_address=VALUES(base_address),
version=VALUES(version),
is_deleted=VALUES(is_deleted);

INSERT INTO repository (paper_id, name, url, description)
VALUES
(1,'Official Implementation','https://github.com/paperswithcode','官方开源实现'),
(2,'Tensor2Tensor','https://github.com/tensorflow/tensor2tensor','社区实现');

INSERT INTO paper_task (paper_id, task_id) VALUES
(1,1),(1,2),(2,3),(2,4);

INSERT INTO paper_dataset (paper_id, dataset_id) VALUES
(1,1),(1,2),(2,3),(2,4);

INSERT INTO result (paper_id, task_id, dataset_id, model_name, metric_name, metric_value, is_sota)
VALUES
(1,1,1,'ResNet-50','Top-1','76.0',0),
(1,2,2,'Faster R-CNN','mAP','42.0',0),
(2,3,3,'Transformer','BLEU','41.8',1);

INSERT INTO folder (folder_id, name, parent_id, user_id)
VALUES
(100,'我的文库',NULL,1),
(101,'精读',100,1),
(102,'略读',100,1),
(103,'计算机视觉',102,1),
(104,'自然语言处理',102,1)
ON DUPLICATE KEY UPDATE name=VALUES(name), parent_id=VALUES(parent_id), user_id=VALUES(user_id);

INSERT INTO folder_paper (folder_id, paper_id)
SELECT 101, p.id FROM paper p WHERE p.title='Deep Learning for Computer Vision: A Comprehensive Survey';
INSERT INTO folder_paper (folder_id, paper_id)
SELECT 102, p.id FROM paper p WHERE p.title='Attention Mechanisms in Natural Language Processing: Recent Advances';
INSERT INTO folder_paper (folder_id, paper_id)
SELECT 103, p.id FROM paper p WHERE p.title='Deep Learning for Computer Vision: A Comprehensive Survey';
INSERT INTO folder_paper (folder_id, paper_id)
SELECT 104, p.id FROM paper p WHERE p.title='Attention Mechanisms in Natural Language Processing: Recent Advances';

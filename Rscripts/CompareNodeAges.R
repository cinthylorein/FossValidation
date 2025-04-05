if (!require("BiocManager", quietly = TRUE))
  install.packages("BiocManager")

BiocManager::install("ggtree")
install.packages("ggplot2")
install.packages("ggimage")
install.packages("igraph")
install.packages("treeio")

library(ggtree)
library(ggplot2)
library(ggimage)
library("igraph")
library(treeio)
library(ggpubr)
library(svglite)
library("tidytree")
library("paleotree")
library(dplyr)
library(tidyr)



################################## CompareNodesAges fuction ##########################

consistency<-function(tree1, tree2, name){
  
  beast1 <- read.beast(tree1)
  beast2 <- read.beast(tree2)
  
  
  
  metadata <- beast1@data
  metadata2 <- beast2@data
  # load dplyr and tidyr library
  library(dplyr)
  library(tidyr)
  
  # Split name column into firstname and last name
  metadata <- metadata %>% separate(height_0.95_HPD, c("value1", "value2"), sep = ",", remove = TRUE) %>%
    mutate(value1 = gsub("^c\\(", "", value1),
           value2 = gsub("\\)", "", value2))
  metadata2 <- metadata2 %>% separate(height_0.95_HPD, c("value1", "value2"), sep = ",", remove = TRUE) %>%
    mutate(value1 = gsub("^c\\(", "", value1),
           value2 = gsub("\\)", "", value2))
  
  metadata <- data.frame(node = metadata$node, height = metadata$height,
                         height_min = metadata$value1, height_max = metadata$value2)
  metadata2 <- data.frame(node = metadata2$node, height_2 = metadata2$height,
                          height_min_2 = metadata2$value1, height_max_2 = metadata2$value2)
  
  
  Asse_Consistency <- merge(metadata, metadata2, by = "node")
  
  
  
  
  #calculate the difference between the molecular fossil age (height) and observed fossil age (height 2) for all other fossil-dated nodes on the tree.
  
  Asse_Consistency$height_Di <- Asse_Consistency$height - Asse_Consistency$height_2
  Asse_Consistency$height_Di_min <- as.numeric(Asse_Consistency$height_min) - as.numeric(Asse_Consistency$height_min_2)
  Asse_Consistency$height_Di_max <- as.numeric(Asse_Consistency$height_max) - as.numeric(Asse_Consistency$height_max_2)
  
  
  Dx <- sum(Asse_Consistency$height_Di)/max(as.numeric(Asse_Consistency$node))
  Dx_min <- sum(Asse_Consistency$height_Di_min)/max(as.numeric(Asse_Consistency$node))
  Dx_max <- sum(Asse_Consistency$height_Di_max)/max(as.numeric(Asse_Consistency$node))  
  data.frame(Dx,Dx_min,Dx_max, fossil=name)
  
}


#First Scenario 

setwd("~/Dropbox/Phd_backup/Thesis/Chapter_3_4/xml/10 genes/First_Scenario_oneperone")

C1_1 <- consistency("starbeast3_COMB_ANN_1FBD.tree", "starbeast3_COMB_ANN_10FBD.tree", "1") 
C1_2 <- consistency("starbeast3_COMB_ANN_2FBD.tree", "starbeast3_COMB_ANN_10FBD.tree", "2") 
C1_3 <- consistency("starbeast3_COMB_ANN_3FBD.tree", "starbeast3_COMB_ANN_10FBD.tree", "3") 
C1_4 <- consistency("starbeast3_COMB_ANN_4FBD.tree", "starbeast3_COMB_ANN_10FBD.tree", "4") 
C1_5 <- consistency("starbeast3_COMB_ANN_5FBD.tree", "starbeast3_COMB_ANN_10FBD.tree", "5") 
C1_6 <- consistency("starbeast3_COMB_ANN_6FBD.tree", "starbeast3_COMB_ANN_10FBD.tree", "6") 
C1_7 <- consistency("starbeast3_COMB_ANN_7FBD.tree", "starbeast3_COMB_ANN_10FBD.tree", "7") 
C1_8 <- consistency("starbeast3_COMB_ANN_8FBD.tree", "starbeast3_COMB_ANN_10FBD.tree", "8") 
C1_9 <- consistency("starbeast3_COMB_ANN_9FBD.tree", "starbeast3_COMB_ANN_10FBD.tree", "9") 
C1_10 <- consistency("starbeast3_COMB_ANN_NO10FBD.tree", "starbeast3_COMB_ANN_10FBD.tree", "10")
C10_10 <- consistency("starbeast3_COMB_ANN_10FBD.tree", "starbeast3_COMB_ANN_10FBD.tree", "10")

total_consistency<- rbind(C1_1,C1_2,C1_3,C1_4,C1_5,C1_6,C1_7,C1_8,C1_9,C1_10,C10_10)

total_consistency$calibrated_nodes <- c("Australidelphia","Dasyuromorphia","Peramelemorphia","Phalangeridae+Burramyidae","Petauridae+Pseudocheiridae",
                                        "Vombatiformes","Macropodidae+Potoroidae","Antechinus+phascogale","Dasyurus+Phascolosorex","Isoodon+Perameles")


category_order <- c("Australidelphia","Dasyuromorphia","Peramelemorphia","Phalangeridae+Burramyidae","Petauridae+Pseudocheiridae",
                    "Vombatiformes","Macropodidae+Potoroidae","Antechinus+phascogale","Dasyurus+Phascolosorex","Isoodon+Perameles")

Asse_Consistency$calibrated_nodes <- factor(Asse_Consistency$calibrated_nodes, levels = category_order)


total_consistency$fossil <- as.numeric(total_consistency$fossil)

# Install and load the latex2exp package
#install.packages("latex2exp")
library(latex2exp)

# Install and load the extrafont package
#install.packages("extrafont")
library(extrafont)

# Load Times New Roman font
font_import(pattern = "times", prompt = FALSE)
loadfonts()

# Set the font family to Times New Roman
theme_set(theme_minimal(base_family = "Times New Roman"))

# Your ggplot code with the modified y-axis label
First <- ggplot(total_consistency, aes(x = fossil, y = Dx)) +
  geom_bar(stat = "identity", fill = "blue", width = 0.8) +
  geom_errorbar(aes(ymin = Dx_min, ymax = Dx_max), width = 0.2) +
  scale_x_continuous(breaks = 1:11, labels = 1:11) +
  labs(title = "First scenario (CompareNodosAges)", 
       x = "Number of fossil calibration nodes per analysis", 
       y = TeX("$\\delta$")) +
  theme(
    text = element_text(size = 16),
    title = element_text(size = 22),
    axis.text = element_text(size = 14),
    axis.title = element_text(size = 18)
  ) +
  geom_hline(yintercept = 0, linetype = "dashed", color = "red")

#Second Scenario 

setwd("~/Documents-cin/Marsupialsanalysis/Second_Scenario")


C1_1 <- consistency("starbeast3_COMB_ANN_1FBD.tree", "starbeast3_COMB_ANN_10FBD.tree", "1") 
C1_2 <- consistency("starbeast3_COMB_ANN_2FBD.tree", "starbeast3_COMB_ANN_10FBD.tree", "2") 
C1_3 <- consistency("starbeast3_COMB_ANN_3FBD.tree", "starbeast3_COMB_ANN_10FBD.tree", "3") 
C1_4 <- consistency("starbeast3_COMB_ANN_4FBD.tree", "starbeast3_COMB_ANN_10FBD.tree", "4") 
C1_5 <- consistency("starbeast3_COMB_ANN_5FBD.tree", "starbeast3_COMB_ANN_10FBD.tree", "5") 
C1_6 <- consistency("starbeast3_COMB_ANN_6FBD.tree", "starbeast3_COMB_ANN_10FBD.tree", "6") 
C1_7 <- consistency("starbeast3_COMB_ANN_7FBD.tree", "starbeast3_COMB_ANN_10FBD.tree", "7") 
C1_8 <- consistency("starbeast3_COMB_ANN_8FBD.tree", "starbeast3_COMB_ANN_10FBD.tree", "8") 
C1_9 <- consistency("starbeast3_COMB_ANN_9FBD.tree", "starbeast3_COMB_ANN_10FBD.tree", "9") 
C1_10 <- consistency("starbeast3_COMB_ANN_10FBD.tree", "starbeast3_COMB_ANN_10FBD.tree", "10")

total_consistency<- rbind(C1_2,C1_3,C1_4,C1_5,C1_6,C1_7,C1_8,C1_9,C1_10)

total_consistency$calibrated_nodes <- c("Dasyuromorphia","Peramelemorphia","Phalangeridae+Burramyidae","Petauridae+Pseudocheiridae",
                                        "Vombatiformes","Macropodidae+Potoroidae","Antechinus+phascogale","Dasyurus+Phascolosorex","Isoodon+Perameles")


category_order <- c("Dasyuromorphia","Peramelemorphia","Phalangeridae+Burramyidae","Petauridae+Pseudocheiridae",
                    "Vombatiformes","Macropodidae+Potoroidae","Antechinus+phascogale","Dasyurus+Phascolosorex","Isoodon+Perameles")

Asse_Consistency$calibrated_nodes <- factor(Asse_Consistency$calibrated_nodes, levels = category_order)


total_consistency$fossil <- as.numeric(total_consistency$fossil)


# Load Times New Roman font
font_import(pattern = "times", prompt = FALSE)
loadfonts()

# Set the font family to Times New Roman
theme_set(theme_minimal(base_family = "Times New Roman"))

# Your ggplot code with the modified y-axis label
Second <- ggplot(total_consistency, aes(x = fossil, y = Dx)) +
  geom_bar(stat = "identity", fill = "blue", width = 0.8) +
  geom_errorbar(aes(ymin = Dx_min, ymax = Dx_max), width = 0.2) +
  scale_x_continuous(breaks = 2:10, labels = 2:10) +
  labs(title = "Second scenario", 
       x = "Number of fossil calibration nodes per analysis", 
       y = TeX("$\\delta$")) +
  theme(
    text = element_text(size = 16),
    title = element_text(size = 22),
    axis.text = element_text(size = 14),
    axis.title = element_text(size = 18)
  ) +
  geom_hline(yintercept = 0, linetype = "dashed", color = "red")


setwd("/Users/cjim882/Dropbox/Phd_backup/Thesis/Chapter_3_4/Figures")


ggsave("CompareNodesAges_FirstScenario.svg", plot = First, width = 6, height = 5, units = "in", dpi = 300)
ggsave("CompareNodesAges_SecondScenario.svg", plot = Second, width = 6, height = 5, units = "in", dpi = 300)


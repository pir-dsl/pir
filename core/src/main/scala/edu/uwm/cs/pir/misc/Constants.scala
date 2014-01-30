package edu.uwm.cs.pir.misc

import edu.uwm.cs.pir.spark.SparkObject._
import edu.uwm.cs.pir.strategy.Strategy._

object Constants {
    
    val DATA_ROOT = inputDataRoot
    val SAMPLES_ROOT = DATA_ROOT + "samples/";
    val SAMPLE_IMAGES_ROOT = SAMPLES_ROOT + "images/";
    val SAMPLE_TEXT_ROOT = SAMPLES_ROOT + "text/";
    val WIKIPEDIA_ROOT = DATA_ROOT + "experiments/early_fusion/wikipedia_dataset/";
    val WIKIPEDIA_IMAGES_ROOT = WIKIPEDIA_ROOT + "images/";
    val WIKIPEDIA_TEXT_ROOT = WIKIPEDIA_ROOT + "texts/";
    
    val STOPWORDS_ROOT = DATA_ROOT + "stoplists/";
    val IMAGE_SERILIZED_FILE_ROOT = DATA_ROOT + "image_features/";
    val EXTRACTED_TEXT_FILE_ROOT = DATA_ROOT + "text_features/";
    val IMAGE_FEATURE_ROOT = DATA_ROOT + "image_features/";
    val CLUSTER_FIlE = DATA_ROOT + "image_cluster/clusters.ser";
    val LDA_MODEL_FIlE = DATA_ROOT + "text_model/lda_model.ser";
    val INDEX_ROOT = DATA_ROOT + "index/";
    val INDEX_IMAGE_FEATURE_ROOT = INDEX_ROOT + "images/";
    val INDEX_TEXT_FEATURE_ROOT = INDEX_ROOT + "text/";
    
    val GROUND_TRUTH_ROOT = DATA_ROOT + "ground_truth/";
    val GROUND_TRUTH_ALL_TEXT_IMAGE_MAP = GROUND_TRUTH_ROOT + "all_txt_img_cat.list";
    val GROUND_TRUTH_TRAINING_TEXT_IMAGE_MAP = GROUND_TRUTH_ROOT + "trainset_txt_img_cat.list";
    val GROUND_TRUTH_TEST_TEXT_IMAGE_MAP = GROUND_TRUTH_ROOT + "testset_txt_img_cat.list";
    val GROUND_TRUTH_CATEGORY_LIST = GROUND_TRUTH_ROOT + "categories.list";

    val DEFAULT_ENCODING = "ISO-8859-1";
    
    val NUM_OF_CLUSTERS = 256;
    val NUM_OF_FEATURES = 300;

    val NUM_OF_TOPICS = 10;
    val ALPHA_SUM = NUM_OF_TOPICS * 0.01;
    val BETA_W = 0.01;

    val SCALE_WIDTH = 320;
    val SCALE_HEIGHT = 240;

    val NUMBER_ITERATION = 1000;
    val NUMBER_SAMPLER = 4;

    // Use Gibbs sampling to infer a topic distribution. Topics are initialized
    // to the (or a) most probable topic for each token. Using zero iterations
    // returns exactly this initial topic distribution. This code does not
    // adjust type-topic counts: P(w|t) is clamped.
    // GIBBS_SAMPLING_ITERATION - The total number of iterations of sampling per
    // document
    // GIBBS_SAMPLING_THINNING - The number of iterations between saved samples
    // GIBBS_SAMPLING_BURNIN - The number of iterations before the first saved
    // sample
    val GIBBS_SAMPLING_ITERATION = 10;
    val GIBBS_SAMPLING_THINNING = 1;
    val GIBBS_SAMPLING_BURNIN = 5;

    var GLOBAL_STRATEGY : RunStrategy = null
}
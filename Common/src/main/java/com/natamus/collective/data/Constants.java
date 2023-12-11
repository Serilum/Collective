package com.natamus.collective.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.natamus.collective.util.CollectiveReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {
    public static final Logger LOG = LoggerFactory.getLogger(CollectiveReference.NAME);
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
}

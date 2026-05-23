#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

struct launch_config {
    char *java;
    char *memory;
    char *native_dir;
    char *classpath;
    char *log;
    char *username;
    char *session;
    char *game_dir;
    char *version;
};

static char *copy_text(const char *value) {
    size_t length;
    char *copy;
    if (value == NULL) {
        value = "";
    }
    length = strlen(value);
    copy = (char *)malloc(length + 1);
    if (copy == NULL) {
        return NULL;
    }
    memcpy(copy, value, length + 1);
    return copy;
}

static char *concat2(const char *left, const char *right) {
    size_t left_length = strlen(left);
    size_t right_length = strlen(right);
    char *out = (char *)malloc(left_length + right_length + 1);
    if (out == NULL) {
        return NULL;
    }
    memcpy(out, left, left_length);
    memcpy(out + left_length, right, right_length + 1);
    return out;
}

static char *concat3(const char *left, const char *middle, const char *right) {
    size_t left_length = strlen(left);
    size_t middle_length = strlen(middle);
    size_t right_length = strlen(right);
    char *out = (char *)malloc(left_length + middle_length + right_length + 1);
    if (out == NULL) {
        return NULL;
    }
    memcpy(out, left, left_length);
    memcpy(out + left_length, middle, middle_length);
    memcpy(out + left_length + middle_length, right, right_length + 1);
    return out;
}

static void free_config(struct launch_config *config) {
    free(config->java);
    free(config->memory);
    free(config->native_dir);
    free(config->classpath);
    free(config->log);
    free(config->username);
    free(config->session);
    free(config->game_dir);
    free(config->version);
}

static int set_field(struct launch_config *config, const char *key, const char *value) {
    char **field = NULL;
    if (strcmp(key, "java") == 0) {
        field = &config->java;
    } else if (strcmp(key, "memory") == 0) {
        field = &config->memory;
    } else if (strcmp(key, "nativeDir") == 0) {
        field = &config->native_dir;
    } else if (strcmp(key, "classpath") == 0) {
        field = &config->classpath;
    } else if (strcmp(key, "log") == 0) {
        field = &config->log;
    } else if (strcmp(key, "username") == 0) {
        field = &config->username;
    } else if (strcmp(key, "session") == 0) {
        field = &config->session;
    } else if (strcmp(key, "gameDir") == 0) {
        field = &config->game_dir;
    } else if (strcmp(key, "version") == 0) {
        field = &config->version;
    }
    if (field == NULL) {
        return 0;
    }
    free(*field);
    *field = copy_text(value);
    return *field == NULL ? -1 : 0;
}

static int read_config(const char *path, struct launch_config *config) {
    FILE *file = fopen(path, "r");
    char line[16384];
    if (file == NULL) {
        fprintf(stderr, "Could not open launch config: %s\n", strerror(errno));
        return 0;
    }
    while (fgets(line, sizeof(line), file) != NULL) {
        char *equals;
        char *end = line + strlen(line);
        while (end > line && (end[-1] == '\n' || end[-1] == '\r')) {
            *--end = '\0';
        }
        if (line[0] == '\0' || line[0] == '#') {
            continue;
        }
        equals = strchr(line, '=');
        if (equals == NULL) {
            continue;
        }
        *equals = '\0';
        if (set_field(config, line, equals + 1) != 0) {
            fclose(file);
            return 0;
        }
    }
    fclose(file);
    return 1;
}

static int has_required_config(const struct launch_config *config) {
    return config->java != NULL
            && config->memory != NULL
            && config->native_dir != NULL
            && config->classpath != NULL
            && config->log != NULL
            && config->username != NULL
            && config->session != NULL
            && config->game_dir != NULL
            && config->version != NULL;
}

int main(int argc, char **argv) {
    struct launch_config config;
    char *xmx;
    char *java_library_path;
    char *lwjgl_library_path;
    char *jinput_library_path;
    char *java_argv[17];
    int index = 0;

    memset(&config, 0, sizeof(config));
    if (argc < 2 || !read_config(argv[1], &config) || !has_required_config(&config)) {
        fprintf(stderr, "MCLauncherRevival macOS game helper could not read its launch config.\n");
        free_config(&config);
        return 64;
    }

    freopen(config.log, "a", stdout);
    freopen(config.log, "a", stderr);

    setenv("MCLR_GAME_USERNAME", config.username, 1);
    setenv("MCLR_GAME_SESSION", config.session, 1);
    setenv("MCLR_GAME_DIR", config.game_dir, 1);
    setenv("MCLR_GAME_VERSION", config.version, 1);

    xmx = concat3("-Xmx", config.memory, "M");
    java_library_path = concat2("-Djava.library.path=", config.native_dir);
    lwjgl_library_path = concat2("-Dorg.lwjgl.librarypath=", config.native_dir);
    jinput_library_path = concat2("-Dnet.java.games.input.librarypath=", config.native_dir);
    if (xmx == NULL || java_library_path == NULL || lwjgl_library_path == NULL || jinput_library_path == NULL) {
        fprintf(stderr, "MCLauncherRevival macOS game helper ran out of memory.\n");
        free_config(&config);
        return 70;
    }

    java_argv[index++] = config.java;
    java_argv[index++] = "-Xdock:name=Minecraft";
    java_argv[index++] = "-Dapple.awt.application.name=Minecraft";
    java_argv[index++] = "-Dapple.awt.UIElement=false";
    java_argv[index++] = "-Djava.awt.headless=false";
    java_argv[index++] = xmx;
    java_argv[index++] = java_library_path;
    java_argv[index++] = lwjgl_library_path;
    java_argv[index++] = jinput_library_path;
    java_argv[index++] = "-cp";
    java_argv[index++] = config.classpath;
    java_argv[index++] = "net.minecraft.MacForegroundMinecraft";
    java_argv[index++] = config.version;
    java_argv[index] = NULL;

    execv(config.java, java_argv);
    fprintf(stderr, "MCLauncherRevival macOS game helper could not start Java: %s\n", strerror(errno));
    free_config(&config);
    free(xmx);
    free(java_library_path);
    free(lwjgl_library_path);
    free(jinput_library_path);
    return 127;
}

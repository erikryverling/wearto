FROM eclipse-temurin:21-jdk-jammy@sha256:9d8dcf999b0bce2453e913823595a5ff2a4e8e9e5d5241b45280d0ff069818ec

# Set environment variables
ENV ANDROID_HOME=/opt/android-sdk \
    ANDROID_SDK_ROOT=/opt/android-sdk \
    PATH=${PATH}:/opt/android-sdk/cmdline-tools/latest/bin:/opt/android-sdk/platform-tools

# Install necessary dependencies
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    git \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Download and install Android Command Line Tools
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    cd ${ANDROID_HOME}/cmdline-tools && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip && \
    unzip commandlinetools-linux-11076708_latest.zip && \
    rm commandlinetools-linux-11076708_latest.zip && \
    mv cmdline-tools latest

# Accept Android licenses and install required SDK components
RUN yes | sdkmanager --licenses || true && \
    sdkmanager "platform-tools" \
    "platforms;android-37.0" \
    "platforms;android-36" \
    "build-tools;37.0.0" \
    "build-tools;36.0.0" \
    "cmdline-tools;latest" && \
    ln -s android-37.0 ${ANDROID_HOME}/platforms/android-37

# Install GitHub CLI
RUN curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg \
  | dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg \
  && echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" \
  | tee /etc/apt/sources.list.d/github-cli.list > /dev/null \
  && apt-get update && apt-get install -y gh \
  && rm -rf /var/lib/apt/lists/*

# Install Node.js for the OpenCode CLI.
RUN curl -fsSL https://deb.nodesource.com/setup_22.x | bash - \
  && apt-get install -y nodejs \
  && rm -rf /var/lib/apt/lists/*

# Install OpenCode CLI (run as root before USER agent)
RUN npm install -g opencode-ai@latest

# Build-args for UID/GID alignment: sandcastle docker build-image
# defaults these to the host user's UID/GID so image-built files
# and bind-mounted files share an owner without runtime chown.
ARG AGENT_UID=1000
ARG AGENT_GID=1000

# Create the agent user and align its UID/GID with the host. The IDs may
# already be used in the base image, so allow non-unique values.
RUN groupadd --non-unique --gid $AGENT_GID agent \
  && useradd --non-unique --uid $AGENT_UID --gid agent --create-home --home-dir /home/agent agent


USER ${AGENT_UID}:${AGENT_GID}

WORKDIR /home/agent

# In worktree sandbox mode, Sandcastle bind-mounts the git worktree at ${SANDBOX_REPO_DIR}
# and overrides the working directory to ${SANDBOX_REPO_DIR} at container start.
# Structure your Dockerfile so that ${SANDBOX_REPO_DIR} can serve as the project root.
ENTRYPOINT ["sleep", "infinity"]

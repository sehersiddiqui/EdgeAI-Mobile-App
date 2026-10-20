# EdgeAI — Reading Without Barriers

<p align="center">
  <img src="https://img.shields.io/badge/AI-Driven-7C3AED?style=for-the-badge" alt="AI Driven"/>
  <img src="https://img.shields.io/badge/Android-Mobile-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android"/>
  <img src="https://img.shields.io/badge/Accessibility-First-0EA5E9?style=for-the-badge" alt="Accessibility First"/>
  <img src="https://img.shields.io/badge/Open%20Source-Community%20Driven-111827?style=for-the-badge&logo=github" alt="Open Source"/>
</p>

<p align="center">
  <strong>Technology should adapt to the reader — not the other way around.</strong>
</p>

<p align="center">
  An AI-powered Android reading companion exploring how deep learning, thoughtful design, and accessible technology can make digital reading more inclusive for people with dyslexia.
</p>

<p align="center">
  <a href="https://github.com/sehersiddiqui/EdgeAI-Mobile-App">Explore the Repository</a>
  ·
  <a href="https://github.com/sehersiddiqui/EdgeAI-Mobile-App/issues">Report an Issue</a>
  ·
  <a href="https://github.com/sehersiddiqui/EdgeAI-Mobile-App/issues">Suggest an Improvement</a>
</p>

---

## ✦ The Idea

Reading is fundamental to learning, communication, and independence. Yet digital reading experiences are rarely designed around the diversity of the people who use them.

For people with dyslexia, dense paragraphs, inconsistent spacing, visually overwhelming layouts, and rigid reading interfaces can make accessing written information unnecessarily difficult.

**EdgeAI explores a different approach: a mobile reading experience that puts accessibility at the centre and artificial intelligence to work in support of the reader.**

Rather than treating accessibility as an optional setting, the project approaches it as a design principle — combining mobile technology, deep-learning-based ideas, and reader-centred interaction to investigate a more inclusive way to experience digital text.

This is not about changing how someone thinks or learns. It is about exploring how technology can better accommodate the way different people read.

## ◈ What We're Building

EdgeAI is an Android application centred on accessible digital reading.

The project brings together three complementary ideas:

| Pillar | Purpose |
| --- | --- |
| **Accessible reading** | Make the reading experience more adaptable, comfortable, and easier to navigate. |
| **Artificial intelligence** | Explore deep-learning techniques that can support reading-related accessibility. |
| **Human-centred design** | Build an interface around clarity, usability, and individual reading preferences. |

The long-term vision is a reading companion that helps users engage with digital content in ways that better suit their needs.

## ✨ Product Vision & Capabilities

The following areas describe the intended product experience. Their implementation status may vary between versions.

### 📚 01 — An Accessible Digital Library

A central place to organise and access reading material, with a focus on making digital content easier to discover and use.

- Organised access to supported reading materials.
- A consistent, reader-focused interface.
- A foundation for expanding accessible content formats.

### 📖 02 — A Customisable Reading Experience

Every reader interacts with text differently. EdgeAI aims to make reading more adaptable rather than imposing a single presentation style.

- Reader-specific visual preferences.
- Typography and spacing customisation where supported.
- Clear navigation and reduced interface clutter.
- An interface designed with accessibility in mind.

### 🧠 03 — Deep Learning for Reading Accessibility

EdgeAI explores the potential of deep learning in assistive reading technology.

The objective is to investigate how AI can contribute to a more responsive reading experience while keeping usability, reliability, and accessibility central to the design.

AI-assisted features should be evaluated against meaningful outcomes rather than assumed to be beneficial simply because they use machine learning.

### 📊 04 — Reading Insights

A future-facing area for presenting useful information about the reading experience, where supported by implemented functionality and appropriate data collection.

The design goal is to make insights understandable and useful without turning reading into a source of pressure or unnecessary performance tracking.

### 📷 05 — Camera-Based Accessibility Exploration

An exploration area for camera-assisted input and accessibility workflows, subject to the capabilities implemented in the application.

Any camera-dependent functionality should provide clear permission handling, appropriate feedback, and an accessible alternative wherever practical.

### 📄 06 — Document Reading

A focus on making supported digital documents easier to access within a mobile environment, with particular attention to readable layouts and intuitive controls.

The supported file formats and document-processing capabilities depend on the current implementation.

---

## 🧭 Design Principles

EdgeAI is guided by a simple belief: assistive technology should empower its users without adding unnecessary complexity.

**Accessibility before aesthetics.** Visual polish matters, but never at the expense of readability, contrast, or usability.

**Personalisation over prescription.** Reading preferences differ. Where possible, users should be able to adapt their experience.

**Clarity over clutter.** Interfaces should make the next action obvious and avoid unnecessary cognitive overhead.

**Responsible AI over AI for its own sake.** Machine learning should serve a defined purpose, be evaluated carefully, and communicate its limitations honestly.

**Privacy as a design consideration.** Camera inputs, reading data, and other potentially sensitive information deserve careful handling.

**Independence over dependency.** Assistive features should support readers' autonomy rather than dictate how they should read.

## 🏗️ Technology & Architecture

EdgeAI is an Android application project with a deep-learning-based accessibility focus.

The repository includes an Android application structure, Gradle build configuration, sample-book resources, and Firestore security rules. <sup>[1](#repository-reference)</sup>

The exact libraries, model architecture, inference pipeline, and backend integrations should be documented against the implementation of the current version.

### Technology areas

| Layer | Role |
| --- | --- |
| Android | Mobile application and user interface |
| Gradle | Build configuration and dependency management |
| Deep learning | Research and development of AI-assisted accessibility capabilities |
| Document processing | Access to supported reading materials |
| Firebase / Firestore | Security-rule configuration is present in the repository; active service integration should be verified |

### Architectural direction

```text
┌─────────────────────────────────────┐
│          Reader Interface           │
│  Library · Reader · Preferences     │
└──────────────────┬──────────────────┘
                   │
┌──────────────────▼──────────────────┐
│       Accessibility Layer           │
│  Readability · Navigation · UX      │
└──────────────────┬──────────────────┘
                   │
┌──────────────────▼──────────────────┐
│        AI / Processing Layer        │
│   Deep Learning · Input Handling    │
└──────────────────┬──────────────────┘
                   │
┌──────────────────▼──────────────────┐
│         Content & Data Layer        │
│ Documents · Local Data · Services   │
└─────────────────────────────────────┘
```
---

## 🚀 Getting Started

Want to explore the project locally? Follow the steps below.

### Prerequisites

Install the following tools:

- [Android Studio](https://developer.android.com/studio)
- [Git](https://git-scm.com/downloads)
- A compatible JDK, Android SDK, and Gradle configuration as required by the project
- An Android device or emulator for testing

### 1. Clone the repository

```bash
git clone https://github.com/sehersiddiqui/EdgeAI-Mobile-App.git
cd EdgeAI-Mobile-App
```

### 2. Open the project

Open Android Studio, select **Open**, and choose the cloned `EdgeAI-Mobile-App` directory.

Allow Android Studio to synchronise the Gradle project and download the required dependencies.

### 3. Configure your environment

Review the project's setup documentation before running the application.

```text
SETUP.md
```

If the application requires Firebase configuration, API credentials, or other external services, configure them locally according to the setup instructions. Never commit private credentials or signing keys to the repository.

### 4. Build the application

On Windows PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

On macOS or Linux:

```bash
./gradlew assembleDebug
```

These commands attempt to build the debug variant. A successful build depends on the installed SDK, compatible Java version, dependencies, and any required project configuration.

### 5. Run on Android

Launch the app using Android Studio's **Run** action with a configured emulator or connected Android device.

Test the actual application on your target Android version before assuming all features are available.

---

## 📁 Repository Structure

The repository currently includes the following top-level structure:

```text
EdgeAI-Mobile-App/
├── app/                 # Android application module
├── gradle/
│   └── wrapper/         # Gradle wrapper configuration
├── sample-books/        # Sample reading materials
├── .gitignore
├── build.gradle         # Root build configuration
├── firestore.rules      # Firestore security rules
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle
├── README.md
└── SETUP.md             # Setup instructions
```

The contents of the application module may evolve as the project develops.

---

## ♿ Accessibility Is the Point

Accessibility is not a single feature, a font choice, or a checkbox in settings.

A genuinely accessible reading application must consider the entire experience — from opening a document to navigating controls, adjusting preferences, recovering from errors, and understanding what the application is doing.

EdgeAI's design direction prioritises the following areas:

- **Readable presentation:** appropriate contrast, legible text, and adaptable layouts.
- **Screen-reader support:** meaningful labels, logical navigation, and accessible interactive controls.
- **Scalable interfaces:** layouts that remain usable with larger text settings and different screen sizes.
- **Predictable interactions:** clear pressed states, feedback, and understandable error messages.
- **Reduced friction:** straightforward navigation and consistent controls.
- **Respectful personalisation:** settings that support individual preferences without judgement.

These are design goals, not a claim of formal accessibility certification. Actual accessibility should be validated through device testing, accessibility audits, and feedback from people with dyslexia and other reading-related needs.

## 🔬 Research & Evaluation

EdgeAI sits at the intersection of mobile development, assistive technology, and applied machine learning.

Building an AI-enabled accessibility application also raises important research questions:

- Which reading adaptations provide measurable benefits to different users?
- How should AI-assisted features be evaluated beyond conventional model accuracy?
- How can accessibility preferences be personalised without making unsupported assumptions about an individual?
- How can assistive functionality remain usable across devices and different levels of connectivity?
- What privacy safeguards are appropriate for reading-related and camera-derived data?

These questions matter because a technically sophisticated application is not automatically an effective assistive technology.

The ultimate goal should be to evaluate whether the application improves usability and reading experience for its intended users, using appropriate methods and evidence.

---

## 🗺️ Roadmap

The following roadmap describes potential development priorities. It is not a statement that these features have already been implemented.

- [ ] **Reading experience** — refine navigation, document presentation, and reader controls.
- [ ] **Personalisation** — expand supported reading preferences and accessibility options.
- [ ] **Inclusive interactions** — strengthen screen-reader support, touch targets, and scalable layouts.
- [ ] **AI evaluation** — document model design, evaluation methodology, limitations, and reproducibility.
- [ ] **Document support** — validate supported file formats and improve document-handling workflows.
- [ ] **Reliability** — test on different Android versions, screen sizes, and device configurations.
- [ ] **Privacy and security** — review permissions, data handling, and security rules.
- [ ] **User-centred evaluation** — seek feedback from intended users and assess accessibility outcomes.

Progress should be reflected in the repository as each item becomes available and is tested.

---

## 🤝 Contributing

Ideas, constructive feedback, accessibility insights, and technical contributions are welcome.

If you'd like to contribute:

1. Explore the existing implementation.
2. Open an issue describing the proposed improvement or problem.
3. Discuss significant changes before beginning substantial implementation work.
4. Submit a pull request with a clear explanation of the change and its validation.

Areas where contributions can be especially valuable include:

- Android development and UI engineering.
- Accessibility audits and assistive technology.
- Deep learning and model evaluation.
- Document processing and reading interfaces.
- Testing, documentation, and usability research.

When contributing accessibility improvements, consider how the change affects the people who will use the application, not just whether the code works.

## 🔐 Privacy & Responsible Use

EdgeAI is intended as an accessibility-oriented software project, not a medical diagnostic tool.

AI-assisted reading functionality should not be presented as a diagnosis of dyslexia or as a replacement for professional assessment.

Before using the application with sensitive documents or personal information, review the implemented data flows and applicable privacy policies. Do not assume that data stays on-device merely because the application is mobile or uses deep learning.

Security, consent, and transparency should remain part of the development process.

## 📜 License

A license was not verified from the repository information available when this README was prepared.

Before distributing or accepting external contributions, add a `LICENSE` file specifying the terms under which the project may be used, modified, and redistributed.

## 👩‍💻 About the Project

**EdgeAI-Mobile-App** is developed by [Seher Siddiqui](https://github.com/sehersiddiqui) and Tanvi Paithankar.

The project explores a simple but important idea: artificial intelligence can be more meaningful when it helps make everyday technology accessible to more people.

If you are interested in Android engineering, deep learning, accessibility, or human-centred AI, you're welcome to explore the repository and contribute to the conversation.

<p align="center">
  <strong>Better technology doesn't ask every reader to adapt.</strong><br/>
  <em>It starts by making room for different ways of reading.</em>
</p>

<p align="center">
  <a href="https://github.com/sehersiddiqui/EdgeAI-Mobile-App">
    <strong>Explore EdgeAI on GitHub →</strong>
  </a>
</p>

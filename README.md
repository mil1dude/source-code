# Eaglercraft 1.21.4 – Web Workspace

🚧 **Warning: Work in Progress**  
This is an experimental workspace aiming to port **Minecraft 1.21.4** to **EaglercraftJS/WASM-GC**, enabling it to run directly in modern web browsers.

**There will only be updates to this repo at least once a week because I do my stuff mostly locally. If you want to see the current stauts look at my webape: [Eaglercraft Client Collection](https://eaglercraft.42web.io/)**

> ⚠️ This version is **not playable yet**. Only the base source code is available.  
> ❌ **Forge mod support is not planned** due to deep engine changes.

---

## 🔍 About

Eaglercraft is a client-side reimplementation of Minecraft that runs entirely in the browser using JavaScript and WebAssembly.  
This repository is a **work-in-progress port of Minecraft 1.21.4**, targeting WASM-GC and modern browser runtimes.

Because of significant changes in the Minecraft internals (e.g. class names, directory structures, rendering internals), this version is **much more complex** than previous Eaglercraft ports.

---

## 🎯 Project Goals

- ✅ Compile and run Minecraft 1.21.4 in the browser (JS/WASM-GC)
- ⚙️ Provide a clean, open development workspace
- ❌ No Forge/mod support planned
- 🔧 Modern WebAssembly toolchain and Gradle-based build system
- 📢 Community contributions welcome via GitHub

---

## 🔧 Build Instructions (early dev phase)

### Requirements:

- Java 21 (e.g. from Adoptium)
- Node.js (for bundling)
- Git + Gradle (Gradle wrapper included)

### Steps:

```bash
git clone https://github.com/mil1dude/eaglercraft-1.21.4.git
cd eaglercraft-1.21.4
./CompileJS
```
The final web build (WASM + JS + HTML) will appear in the build/ directory.

Build scripts are not final – changes expected frequently.

## 🌐 Live Versions

Looking to play right now?  
Fully working Eaglercraft versions are available here:

- ✅ **[Play Eaglercraft 1.8.8](https://eaglercraft.42web.io/clients/1.8.8/)**
- ✅ **[Play Eaglercraft 1.12.2](https://eaglercraft.42web.io/clients/1.12.2/)**

The 1.21.4 version is currently only available as source code and **not yet playable**.

---

## 🤝 Contributing

This project is open for contributions.  
If you'd like to help port, fix, test, or improve any part of the codebase:

1. Fork the repository
2. Create a feature or fix branch
3. Submit a pull request

Please open an issue beforehand for large features or structural changes.

---

## 📄 License & Disclaimer

This project includes modified decompiled code from **Minecraft by Mojang Studios**, which is © Mojang and Microsoft.  
It is published for **educational and preservation purposes only**.

Do not use this code for commercial purposes or to distribute official Minecraft assets.

---

## 🧑‍💻 Maintainer

**mil1dude**  
🔗 Website: [https://eaglercraft.42web.io](https://eaglercraft.42web.io)  
📧 GitHub: [github.com/mil1dude](https://github.com/mil1dude)

---

> Stay tuned – the project is progressing slowly but steadily.

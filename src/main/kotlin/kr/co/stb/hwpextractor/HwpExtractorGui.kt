package kr.co.stb.hwpextractor

import com.formdev.flatlaf.FlatLightLaf
import com.formdev.flatlaf.FlatDarkLaf
import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.*
import java.io.File
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.CompoundBorder
import javax.swing.filechooser.FileNameExtensionFilter

class HwpExtractorGui : JFrame("HWP/HWPX 텍스트 추출기 v$VERSION") {

    private val fileListModel = DefaultListModel<String>()
    private val fileList = JList(fileListModel)
    private val outputArea = JTextArea()
    private val progressBar = JProgressBar()

    private val debugCheckBox = JCheckBox("디버그 모드", false)
    private val extractMetaCheckBox = JCheckBox("메타데이터 추출", false)
    private val extractFilesCheckBox = JCheckBox("내장 파일 추출", false)
    private val outputToConsoleCheckBox = JCheckBox("콘솔로 출력", false)
    private val useCurrentDirCheckBox = JCheckBox("파일과 같은 폴더에 저장", true)
    private val passwordField = JPasswordField(20)
    private val outputDirField = JTextField(30)

    init {
        setupMenuBar()
        setupUI()
        setupDragAndDrop()

        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(950, 650)
        setLocationRelativeTo(null)
        minimumSize = Dimension(800, 550)
    }

    private fun setupMenuBar() {
        val menuBar = JMenuBar()

        val viewMenu = JMenu("보기")
        val themeMenu = JMenu("테마")

        val lightThemeItem = JMenuItem("밝은 테마").apply {
            addActionListener {
                switchTheme(true)
            }
        }

        val darkThemeItem = JMenuItem("어두운 테마").apply {
            addActionListener {
                switchTheme(false)
            }
        }

        themeMenu.add(lightThemeItem)
        themeMenu.add(darkThemeItem)
        viewMenu.add(themeMenu)
        menuBar.add(viewMenu)

        // About 메뉴
        val helpMenu = JMenu("도움말")
        val aboutItem = JMenuItem("정보").apply {
            addActionListener {
                showAboutDialog()
            }
        }
        helpMenu.add(aboutItem)
        menuBar.add(helpMenu)

        jMenuBar = menuBar
    }

    private fun showAboutDialog() {
        val aboutMessage = """
            <html>
            <body style='width: 300px; padding: 10px;'>
            <h2 style='margin-top: 0;'>Hwp2Text</h2>
            <p style='margin: 10px 0;'>HWP/HWPX 파일 텍스트 추출 도구</p>
            <p style='margin: 10px 0;'>버전: $VERSION</p>
            <hr style='margin: 15px 0;'>
            <p style='margin: 10px 0;'><b>테레비 - 개발관련자료:</b></p>
            <p style='margin: 5px 0;'><a href='https://terebee.tistory.com/'>https://terebee.tistory.com/</a></p>
            </body>
            </html>
        """.trimIndent()

        val editorPane = JEditorPane("text/html", aboutMessage).apply {
            isEditable = false
            isOpaque = false
            addHyperlinkListener { e ->
                if (e.eventType == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        Desktop.getDesktop().browse(e.url.toURI())
                    } catch (ex: Exception) {
                        JOptionPane.showMessageDialog(
                            this@HwpExtractorGui,
                            "브라우저를 열 수 없습니다: ${e.url}",
                            "오류",
                            JOptionPane.ERROR_MESSAGE
                        )
                    }
                }
            }
        }

        JOptionPane.showMessageDialog(
            this,
            editorPane,
            "정보",
            JOptionPane.INFORMATION_MESSAGE
        )
    }

    private fun switchTheme(isLight: Boolean) {
        try {
            if (isLight) {
                FlatLightLaf.setup()
            } else {
                FlatDarkLaf.setup()
            }
            SwingUtilities.updateComponentTreeUI(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupUI() {
        layout = BorderLayout(15, 15)
        (contentPane as JComponent).border = EmptyBorder(15, 15, 15, 15)

        // Top panel - Options with modern design
        val optionsPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = CompoundBorder(
                BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color(70, 130, 180), 2),
                    " 옵션 ",
                    javax.swing.border.TitledBorder.LEFT,
                    javax.swing.border.TitledBorder.TOP,
                    Font("맑은 고딕", Font.BOLD, 13)
                ),
                EmptyBorder(10, 15, 15, 15)
            )

            // Checkboxes row
            val checkboxPanel = JPanel(FlowLayout(FlowLayout.LEFT, 15, 5)).apply {
                add(debugCheckBox)
                add(extractMetaCheckBox)
                add(extractFilesCheckBox)
                add(outputToConsoleCheckBox)
            }
            add(checkboxPanel)

            add(Box.createVerticalStrut(10))

            // Password row
            val passwordPanel = JPanel(FlowLayout(FlowLayout.LEFT, 10, 5)).apply {
                add(JLabel("비밀번호:").apply {
                    font = Font("맑은 고딕", Font.PLAIN, 12)
                })
                passwordField.preferredSize = Dimension(250, 28)
                add(passwordField)
            }
            add(passwordPanel)

            add(Box.createVerticalStrut(10))

            // Output directory row
            val outputPanel = JPanel(FlowLayout(FlowLayout.LEFT, 10, 5)).apply {
                add(JLabel("출력 폴더:").apply {
                    font = Font("맑은 고딕", Font.PLAIN, 12)
                })
                outputDirField.preferredSize = Dimension(300, 28)
                outputDirField.isEnabled = false
                add(outputDirField)

                val browseBtn = JButton("찾아보기...").apply {
                    preferredSize = Dimension(100, 28)
                    addActionListener { browseOutputDirectory() }
                    isEnabled = false
                }
                add(browseBtn)

                add(Box.createHorizontalStrut(10))

                useCurrentDirCheckBox.addActionListener {
                    val enabled = !useCurrentDirCheckBox.isSelected && !outputToConsoleCheckBox.isSelected
                    outputDirField.isEnabled = enabled
                    browseBtn.isEnabled = enabled
                    if (!enabled) {
                        outputDirField.text = ""
                    }
                }
                add(useCurrentDirCheckBox)
            }
            add(outputPanel)

            // 콘솔 출력 체크박스 이벤트 리스너
            outputToConsoleCheckBox.addActionListener {
                val consoleMode = outputToConsoleCheckBox.isSelected
                useCurrentDirCheckBox.isEnabled = !consoleMode
                outputDirField.isEnabled = !consoleMode && !useCurrentDirCheckBox.isSelected
                val browseBtn = outputPanel.components.find { it is JButton && it.text == "찾아보기..." } as? JButton
                browseBtn?.isEnabled = !consoleMode && !useCurrentDirCheckBox.isSelected
                if (consoleMode) {
                    outputDirField.text = ""
                }
            }
        }

        add(optionsPanel, BorderLayout.NORTH)

        // Center panel - Split pane with file list and output
        val centerPanel = JSplitPane(JSplitPane.HORIZONTAL_SPLIT).apply {
            dividerLocation = 350
            resizeWeight = 0.4

            // Left - File list with modern styling
            val filePanel = JPanel(BorderLayout(10, 10)).apply {
                border = CompoundBorder(
                    BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color(100, 149, 237), 2),
                        " 처리할 파일 목록 (드래그 앤 드롭 지원) ",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        Font("맑은 고딕", Font.BOLD, 12)
                    ),
                    EmptyBorder(10, 10, 10, 10)
                )

                fileList.apply {
                    selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
                    font = Font("맑은 고딕", Font.PLAIN, 11)
                    visibleRowCount = 10
                }
                add(JScrollPane(fileList).apply {
                    border = BorderFactory.createLineBorder(Color.GRAY, 1)
                }, BorderLayout.CENTER)

                val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER, 10, 5)).apply {
                    add(JButton("+ 파일 추가").apply {
                        preferredSize = Dimension(110, 32)
                        font = Font("맑은 고딕", Font.PLAIN, 11)
                        addActionListener { addFiles() }
                    })
                    add(JButton("- 선택 제거").apply {
                        preferredSize = Dimension(110, 32)
                        font = Font("맑은 고딕", Font.PLAIN, 11)
                        addActionListener { removeSelectedFiles() }
                    })
                    add(JButton("x 전체 삭제").apply {
                        preferredSize = Dimension(110, 32)
                        font = Font("맑은 고딕", Font.PLAIN, 11)
                        addActionListener { fileListModel.clear() }
                    })
                }
                add(buttonPanel, BorderLayout.SOUTH)
            }

            // Right - Output area with modern styling
            val outputPanel = JPanel(BorderLayout(10, 10)).apply {
                border = CompoundBorder(
                    BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color(60, 179, 113), 2),
                        " 출력 로그 ",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        Font("맑은 고딕", Font.BOLD, 12)
                    ),
                    EmptyBorder(10, 10, 10, 10)
                )

                outputArea.apply {
                    isEditable = false
                    font = Font("맑은 고딕", Font.PLAIN, 11)
                    lineWrap = false
                }
                add(JScrollPane(outputArea).apply {
                    border = BorderFactory.createLineBorder(Color.GRAY, 1)
                }, BorderLayout.CENTER)

                val clearBtn = JButton("로그 지우기").apply {
                    preferredSize = Dimension(120, 32)
                    font = Font("맑은 고딕", Font.PLAIN, 11)
                    addActionListener { outputArea.text = "" }
                }
                val btnPanel = JPanel(FlowLayout(FlowLayout.CENTER))
                btnPanel.add(clearBtn)
                add(btnPanel, BorderLayout.SOUTH)
            }

            leftComponent = filePanel
            rightComponent = outputPanel
        }

        add(centerPanel, BorderLayout.CENTER)

        // Bottom panel - Progress and extract button with modern styling
        val bottomPanel = JPanel(BorderLayout(15, 10)).apply {
            border = CompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, Color(100, 100, 100)),
                EmptyBorder(15, 10, 10, 10)
            )

            progressBar.apply {
                isStringPainted = true
                string = "파일 처리 준비 완료"
                preferredSize = Dimension(0, 35)
                font = Font("맑은 고딕", Font.PLAIN, 12)
            }
            add(progressBar, BorderLayout.CENTER)

            val extractBtn = JButton("▶ 텍스트 추출").apply {
                font = Font("맑은 고딕", Font.BOLD, 14)
                preferredSize = Dimension(180, 45)
                background = Color(34, 139, 34)
                foreground = Color.WHITE
                isFocusPainted = false
                addActionListener { extractFiles() }
            }
            add(extractBtn, BorderLayout.EAST)
        }

        add(bottomPanel, BorderLayout.SOUTH)
    }

    private fun setupDragAndDrop() {
        DropTarget(fileList, object : DropTargetAdapter() {
            override fun drop(evt: DropTargetDropEvent) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY)
                    val droppedFiles = evt.transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>

                    droppedFiles.forEach { file ->
                        if (file is File && file.isFile) {
                            val ext = file.extension.lowercase()
                            if (ext == "hwp" || ext == "hwpx") {
                                if (!fileListModel.contains(file.absolutePath)) {
                                    fileListModel.addElement(file.absolutePath)
                                }
                            }
                        }
                    }

                    evt.dropComplete(true)
                } catch (e: Exception) {
                    e.printStackTrace()
                    evt.dropComplete(false)
                }
            }
        })
    }

    private fun addFiles() {
        val chooser = JFileChooser().apply {
            isMultiSelectionEnabled = true
            fileFilter = FileNameExtensionFilter("HWP/HWPX Files", "hwp", "hwpx")
        }

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            chooser.selectedFiles.forEach { file ->
                if (!fileListModel.contains(file.absolutePath)) {
                    fileListModel.addElement(file.absolutePath)
                }
            }
        }
    }

    private fun removeSelectedFiles() {
        val selected = fileList.selectedIndices
        for (i in selected.indices.reversed()) {
            fileListModel.remove(selected[i])
        }
    }

    private fun browseOutputDirectory() {
        val chooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            if (outputDirField.text.isNotBlank()) {
                currentDirectory = File(outputDirField.text)
            }
        }

        if (chooser.showDialog(this, "선택") == JFileChooser.APPROVE_OPTION) {
            outputDirField.text = chooser.selectedFile.absolutePath
        }
    }

    private fun extractFiles() {
        if (fileListModel.isEmpty) {
            JOptionPane.showMessageDialog(
                this,
                "처리할 파일을 추가해주세요",
                "파일 없음",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        // Run extraction in background thread
        object : SwingWorker<Void, String>() {
            override fun doInBackground(): Void? {
                val password = if (passwordField.password.isNotEmpty())
                    String(passwordField.password) else null

                // Determine output directory based on checkbox
                val outputDir = when {
                    useCurrentDirCheckBox.isSelected -> null  // Use file's directory
                    outputDirField.text.isNotBlank() -> outputDirField.text
                    else -> null
                }

                val extractor = HwpExtractor(
                    debug = debugCheckBox.isSelected,
                    extractMeta = extractMetaCheckBox.isSelected,
                    extractFiles = extractFilesCheckBox.isSelected,
                    outputDirectory = outputDir,
                    password = password,
                    outputToConsole = outputToConsoleCheckBox.isSelected
                )

                val total = fileListModel.size()
                var processed = 0

                for (i in 0 until fileListModel.size()) {
                    val filePath = fileListModel.getElementAt(i)
                    val fileName = File(filePath).name

                    publish("처리 중: $fileName\n")
                    progressBar.value = (processed * 100) / total
                    progressBar.string = "처리 중 ${processed + 1}/$total"

                    try {
                        extractor.extract(filePath)
                        publish("→ 성공: $fileName\n")
                    } catch (e: Exception) {
                        publish("x 오류: $fileName - ${e.message}\n")
                        if (debugCheckBox.isSelected) {
                            publish("  ${e.stackTraceToString()}\n")
                        }
                    }

                    processed++
                }

                return null
            }

            override fun process(chunks: List<String>) {
                chunks.forEach { outputArea.append(it) }
                outputArea.caretPosition = outputArea.document.length
            }

            override fun done() {
                progressBar.value = 100
                progressBar.string = "완료"
                JOptionPane.showMessageDialog(
                    this@HwpExtractorGui,
                    "텍스트 추출이 완료되었습니다!",
                    "완료",
                    JOptionPane.INFORMATION_MESSAGE
                )
            }
        }.execute()
    }
}

fun launchGui() {
    // Set modern FlatLaf light theme
    try {
        FlatLightLaf.setup()
        UIManager.put("Button.arc", 8)
        UIManager.put("Component.arc", 8)
        UIManager.put("TextComponent.arc", 8)
        UIManager.put("ProgressBar.arc", 8)
    } catch (e: Exception) {
        e.printStackTrace()
        // Fallback to system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (e2: Exception) {
            e2.printStackTrace()
        }
    }

    SwingUtilities.invokeLater {
        HwpExtractorGui().isVisible = true
    }
}

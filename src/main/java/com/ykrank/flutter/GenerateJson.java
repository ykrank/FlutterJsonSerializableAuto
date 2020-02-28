package com.ykrank.flutter;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SyntheticElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerateJson extends AnAction {
    private Editor mEditor;
    private Project mProject;
    private Document mDocument;
    private CaretModel mCaret;
    private PsiFile mFile;

    private String JSON_PACKAGE_IMPORT;
    private String PART_IMPORT;
    private String ANNOTATION;
    private String JSON_METHOD;
    private String mFileName = "";
    private String mClassName = "";
    private String mFileContent = "";
    private int mClassLine = -1;
    private int mLastImportLine = -1;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        initParams(e);
        if (mFile == null) {
            return;
        }
        if (mFileContent == null || mFileContent.equals("")) {
            return;
        }
        if (mClassName != null && !mClassName.equals("") && mClassLine >= 0) {
            JSON_PACKAGE_IMPORT = "import 'package:json_annotation/json_annotation.dart';";
            PART_IMPORT = "part '" + mFileName + ".g.dart';";
            ANNOTATION = "@JsonSerializable()\n";
            JSON_METHOD = "     " + mClassName + "();\n\n" +
                    "    factory " + mClassName
                    + ".fromJson(Map<String, dynamic> json) => _$" + mClassName
                    + "FromJson(json);\n\n"
                    + "     Map<String, dynamic> toJson() => _$"
                    + mClassName
                    + "ToJson(this);\n\n";

            int addLine = 1;
            mCaret.moveToVisualPosition(new VisualPosition(mClassLine, 0));
            WriteCommandAction.runWriteCommandAction(mProject, () -> {
                mDocument.insertString(mCaret.getOffset(), ANNOTATION);
            });
            addLine += 1;

            if (!mFileContent.contains(PART_IMPORT)) {
                addLine += 2;
                int line = Math.max(0, Math.min(mLastImportLine + 1, mClassLine - 1));
                mCaret.moveToVisualPosition(new VisualPosition(line, 0));
                WriteCommandAction.runWriteCommandAction(mProject, () -> {
                    mDocument.insertString(mCaret.getOffset(), PART_IMPORT + "\n\n");
                });
            }

            if (!mFileContent.contains(JSON_PACKAGE_IMPORT)) {
                addLine += 2;
                WriteCommandAction.runWriteCommandAction(mProject, () -> {
                    mDocument.insertString(0, JSON_PACKAGE_IMPORT
                            + "\n\n");
                });
            }

            mCaret.moveToVisualPosition(new VisualPosition(mClassLine + addLine, 0));
            int offset = mCaret.getOffset();
            WriteCommandAction.runWriteCommandAction(mProject, () -> {
                mDocument.insertString(offset == -1 ? 0 : offset, JSON_METHOD);
            });
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setEnabled(false);
        Project project = e.getProject();
        if (project != null) {
            Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
            PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
            if (file != null) {
                String fileName = file.getName();
                if (fileName.contains(".dart")) {
                    e.getPresentation().setEnabled(true);
                }
            }
        }
    }

    private void initParams(AnActionEvent e) {
        mEditor = e.getRequiredData(CommonDataKeys.EDITOR);
        mProject = e.getProject();
        mDocument = mEditor.getDocument();
        mCaret = mEditor.getCaretModel();
        mFile = PsiUtilBase.getPsiFileInEditor(mEditor, mProject);
        if (mFile == null) {
            return;
        }
        mFileName = mFile.getName();
        int dotIndex = mFileName.indexOf('.');
        mFileName = mFileName.substring(0, dotIndex);
        mFileContent = mDocument.getText();
        parseClassName(mFileContent);
    }

    private void parseClassName(String fileContent) {
        mClassName = null;
        mClassLine = -1;
        ///获取当前编辑页面中className。如果有多个，选择光标之上最近的一个，如果之上没有，选择下面最近的一个。
        if (fileContent == null || fileContent.equals("")) {
            return;
        }

        String regex = "class(.*?)(extends(.*?))?(implements(.*?))?\\{";
        Pattern pattern = Pattern.compile(regex);// 匹配的模式
        Matcher m = pattern.matcher(fileContent);
        List<String> list = new ArrayList<>();
        List<Integer> startList = new ArrayList<>();
        SelectionModel selectionModel = mEditor.getSelectionModel();
        int offset = selectionModel.getSelectionStart();
        while (m.find()) {
            list.add(m.group(1).trim());
            startList.add(m.start());
        }
        if (list.size() > 0) {
            int chooseIndex = 0;
            for (int i = 0; i < list.size(); i++) {
                if(startList.get(i) >= offset){
                    break;
                }
                chooseIndex = i;
            }
            mClassName = list.get(chooseIndex);
            int mOffset = startList.get(chooseIndex);
            mClassLine = mDocument.getLineNumber(mOffset);
        }

        mLastImportLine = fileContent.lastIndexOf("import ");
    }

    private void showInfoDialog(String message) {
        Messages.showErrorDialog(message, "Info");
    }
}
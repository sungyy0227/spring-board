import Editor from "@toast-ui/editor";
import "@toast-ui/editor/dist/toastui-editor.css";
import { forwardRef, useEffect, useImperativeHandle, useRef, useState } from "react";
import { uploadEditorImage, type EditorImageResponse } from "../api/images";
import "./PostEditor.css";

export interface PostEditorHandle {
  getContent(): string;
  getUsedImageIds(): number[];
  isUploading(): boolean;
}

interface PostEditorProps {
  initialValue?: string;
}

const PostEditor = forwardRef<PostEditorHandle, PostEditorProps>(function PostEditor(
  { initialValue = "" },
  ref,
) {
  const editorElementRef = useRef<HTMLDivElement>(null);
  const editorRef = useRef<Editor | null>(null);
  const uploadedImagesRef = useRef<EditorImageResponse[]>([]);
  const pendingUploadCountRef = useRef(0);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadErrorMessage, setUploadErrorMessage] = useState<string | null>(null);

  useImperativeHandle(ref, () => ({
    getContent() {
      const editor = editorRef.current;

      if (editor === null || editor.getMarkdown().trim() === "") {
        return "";
      }

      return editor.getHTML();
    },
    getUsedImageIds() {
      const content = editorRef.current?.getHTML() ?? "";

      return uploadedImagesRef.current
        .filter((image) => content.includes(image.url))
        .map((image) => image.imageId);
    },
    isUploading() {
      return pendingUploadCountRef.current > 0;
    },
  }), []);

  useEffect(() => {
    const editorElement = editorElementRef.current;

    if (editorElement === null) {
      return;
    }

    const editor = new Editor({
      el: editorElement,
      height: "500px",
      initialEditType: "wysiwyg",
      previewStyle: "vertical",
      placeholder: "내용을 입력하세요",
      usageStatistics: false,
      autofocus: false,
      initialValue,
      hooks: {
        addImageBlobHook: async (blob, callback) => {
          pendingUploadCountRef.current += 1;
          setIsUploading(true);
          setUploadErrorMessage(null);

          try {
            const uploadedImage = await uploadEditorImage(blob);
            uploadedImagesRef.current.push(uploadedImage);

            const altText = blob instanceof File && blob.name
              ? blob.name
              : "image";
            callback(uploadedImage.url, altText);
          } catch (error) {
            if (error instanceof Error) {
              setUploadErrorMessage(error.message);
            } else {
              setUploadErrorMessage("이미지 업로드 중 오류가 발생했습니다.");
            }
          } finally {
            pendingUploadCountRef.current -= 1;
            setIsUploading(pendingUploadCountRef.current > 0);
          }
        },
      },
    });

    editorRef.current = editor;

    return () => {
      editor.destroy();
      editorRef.current = null;
    };
  }, [initialValue]);

  return (
    <div className="post-editor">
      <div ref={editorElementRef} />

      {isUploading && (
        <p className="editor-status" aria-live="polite">
          이미지를 업로드하고 있습니다...
        </p>
      )}

      {uploadErrorMessage !== null && (
        <p className="editor-error" role="alert">
          {uploadErrorMessage}
        </p>
      )}
    </div>
  );
});

export default PostEditor;

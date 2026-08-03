import { useEffect } from 'react';
import { useLexicalComposerContext } from '@lexical/react/LexicalComposerContext';
import {
    $insertNodes,
    $createParagraphNode,
    $isRootOrShadowRoot,
    COMMAND_PRIORITY_EDITOR,
    createCommand,
} from 'lexical';
import { $createImageNode, ImageNode } from './ImageNode.jsx';

export const INSERT_IMAGE_COMMAND = createCommand('INSERT_IMAGE_COMMAND');

/**
 * 이미지 삽입 플러그인.
 * INSERT_IMAGE_COMMAND({ src, altText }) 를 dispatch 하면
 * 커서 위치에 ImageNode를 삽입하고, 루트 직속이면 paragraph로 감쌉니다.
 */
export function ImagePlugin() {
    const [editor] = useLexicalComposerContext();

    useEffect(() => {
        if (!editor.hasNodes([ImageNode])) {
            throw new Error('ImagePlugin: ImageNode가 Composer에 등록되지 않았습니다.');
        }

        return editor.registerCommand(
            INSERT_IMAGE_COMMAND,
            ({ src, altText = '' }) => {
                const imageNode = $createImageNode(src, altText);
                $insertNodes([imageNode]);

                // 루트 바로 아래에 있으면 paragraph로 감싸기
                const parent = imageNode.getParentOrThrow();
                if ($isRootOrShadowRoot(parent)) {
                    const paragraph = $createParagraphNode();
                    parent.append(paragraph);
                    paragraph.select();
                } else {
                    // 이미지 뒤에 커서를 paragraph로 이동
                    const paragraphAfter = $createParagraphNode();
                    imageNode.insertAfter(paragraphAfter);
                    paragraphAfter.select();
                }

                return true;
            },
            COMMAND_PRIORITY_EDITOR,
        );
    }, [editor]);

    return null;
}

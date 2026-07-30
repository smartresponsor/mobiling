import { Editor } from '@tiptap/core'
import StarterKit from '@tiptap/starter-kit'
import Link from '@tiptap/extension-link'
import './style.css'

type Bridge = { postMessage?: (message: string) => void }

declare global {
  interface Window {
    AndroidRichText?: Bridge
    webkit?: { messageHandlers?: { richText?: Bridge } }
    MobilingRichText?: {
      setContent: (json: unknown) => void
      getContent: () => unknown
      focus: () => void
    }
  }
}

const bridge = (): Bridge | undefined =>
  window.AndroidRichText ?? window.webkit?.messageHandlers?.richText

const emit = (type: string, payload: unknown) =>
  bridge()?.postMessage?.(JSON.stringify({ type, payload }))

const editor = new Editor({
  element: document.querySelector('#editor') as HTMLElement,
  extensions: [
    StarterKit.configure({ heading: { levels: [2, 3] } }),
    Link.configure({ openOnClick: false, autolink: true, linkOnPaste: true }),
  ],
  content: { type: 'doc', content: [{ type: 'paragraph' }] },
  editorProps: {
    attributes: {
      class: 'ProseMirror',
      'aria-label': 'Project story editor',
      spellcheck: 'true',
    },
  },
  onUpdate: ({ editor: instance }) => publish(instance),
  onSelectionUpdate: () => renderToolbar(),
})

const commands = [
  ['B', 'Bold', () => editor.chain().focus().toggleBold().run(), () => editor.isActive('bold')],
  ['I', 'Italic', () => editor.chain().focus().toggleItalic().run(), () => editor.isActive('italic')],
  ['H2', 'Heading', () => editor.chain().focus().toggleHeading({ level: 2 }).run(), () => editor.isActive('heading', { level: 2 })],
  ['•', 'Bullet list', () => editor.chain().focus().toggleBulletList().run(), () => editor.isActive('bulletList')],
  ['1.', 'Numbered list', () => editor.chain().focus().toggleOrderedList().run(), () => editor.isActive('orderedList')],
  ['“', 'Quote', () => editor.chain().focus().toggleBlockquote().run(), () => editor.isActive('blockquote')],
  ['↶', 'Undo', () => editor.chain().focus().undo().run(), () => false],
  ['↷', 'Redo', () => editor.chain().focus().redo().run(), () => false],
] as const

function publish(instance: Editor) {
  const text = instance.getText()
  const words = text.trim() ? text.trim().split(/\s+/).length : 0
  document.querySelector('#word-count')!.textContent = `${words} ${words === 1 ? 'word' : 'words'}`
  emit('change', { json: instance.getJSON(), html: instance.getHTML(), text })
}

function renderToolbar() {
  const toolbar = document.querySelector('#toolbar')!
  toolbar.innerHTML = ''
  for (const [label, title, run, active] of commands) {
    const button = document.createElement('button')
    button.type = 'button'
    button.textContent = label
    button.title = title
    button.setAttribute('aria-label', title)
    button.className = active() ? 'active' : ''
    button.addEventListener('click', run)
    toolbar.appendChild(button)
  }

  const link = document.createElement('button')
  link.type = 'button'
  link.textContent = '🔗'
  link.title = 'Link'
  link.setAttribute('aria-label', 'Link')
  link.className = editor.isActive('link') ? 'active' : ''
  link.addEventListener('click', () => {
    const current = editor.getAttributes('link').href as string | undefined
    const url = window.prompt('Link URL', current ?? 'https://')
    if (url === null) return
    if (!url.trim()) editor.chain().focus().unsetLink().run()
    else editor.chain().focus().extendMarkRange('link').setLink({ href: url.trim() }).run()
  })
  toolbar.appendChild(link)
}

window.MobilingRichText = {
  setContent: json => {
    editor.commands.setContent(json ?? { type: 'doc', content: [{ type: 'paragraph' }] })
    publish(editor)
  },
  getContent: () => editor.getJSON(),
  focus: () => editor.commands.focus(),
}

renderToolbar()
publish(editor)
emit('ready', { schemaVersion: 1 })

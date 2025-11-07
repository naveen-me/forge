<template>
<div class="p-6 bg-background-light dark:bg-background-dark font-sans">
<div class="flex flex-col md:flex-row justify-between md:items-center mb-4 gap-3">
<h1 class="text-2xl font-bold text-text-light dark:text-text-dark">Library</h1>
<div class="flex flex-col md:flex-row gap-2">
<button @click="createNewFolder" class="flex items-center justify-center gap-2 px-3 py-2 rounded-md border border-border-light dark:border-border-dark bg-card-light dark:bg-card-dark text-text-light dark:text-text-dark text-sm">
<span class="material-icons text-base">create_new_folder</span>
<span>New Folder</span>
</button>
<button @click="addFiles" class="flex items-center justify-center gap-2 px-3 py-2 rounded-md bg-primary text-white text-sm">
<span class="material-icons text-base">add</span>
<span>Add Files</span>
</button>
</div>
</div>
<div class="mb-4">
<nav aria-label="Breadcrumb" class="flex">
<ol class="inline-flex items-center space-x-1 md:space-x-2 rtl:space-x-reverse">
<li class="inline-flex items-center">
<a @click="navigateToFolder(null)" class="inline-flex items-center text-xs font-medium text-subtext-light hover:text-primary dark:text-subtext-dark dark:hover:text-white cursor-pointer">
<span class="material-icons text-base mr-1.5">folder</span> Media Library
                        </a>
</li>
<li v-for="(folder, index) in currentPath" :key="index">
<div class="flex items-center">
<span class="material-icons text-subtext-light dark:text-subtext-dark text-base">chevron_right</span>
<a @click="navigateToBreadcrumb(index)" class="ms-1 text-xs font-medium text-subtext-light hover:text-primary md:ms-2 dark:text-subtext-dark dark:hover:text-white cursor-pointer">
{{ folder.name }}
</a>
</div>
</li>
</ol>
</nav>
</div>
<div class="flex flex-col md:flex-row gap-4 mb-4">
<div class="w-full flex-grow flex flex-col sm:flex-row gap-2">
<div class="relative flex-grow">
<span class="material-icons absolute left-3 top-1/2 -translate-y-1/2 text-subtext-light dark:text-subtext-dark text-lg">search</span>
<input v-model="searchQuery" @input="searchItems" class="w-full pl-10 pr-4 py-2 rounded-md border border-border-light dark:border-border-dark bg-card-light dark:bg-card-dark text-text-light dark:text-text-dark focus:ring-primary focus:border-primary text-sm h-full" placeholder="Search" type="text"/>
</div>
<div class="flex gap-2 justify-end">
<div class="relative">
<button @click="sortOpen = !sortOpen" class="flex items-center gap-2 px-3 py-2 rounded-md border border-border-light dark:border-border-dark bg-card-light dark:bg-card-dark text-text-light dark:text-text-dark text-sm h-full">
<span class="material-icons text-base">sort</span>
<span>Sort By</span>
<span class="material-icons text-base">expand_more</span>
</button>
<div v-if="sortOpen" @click.outside="sortOpen = false" class="absolute z-10 mt-1 w-56 bg-card-light dark:bg-card-dark rounded-lg shadow-lg border border-border-light dark:border-border-dark">
<ul class="py-1 text-sm">
<li><a @click="setSort('name', 'asc')" class="flex items-center justify-between px-3 py-1.5 hover:bg-gray-100 dark:hover:bg-gray-700 text-text-light dark:text-text-dark cursor-pointer"><span class="flex items-center gap-2"><span class="material-icons text-base">arrow_upward</span> Name</span></a></li>
<li><a @click="setSort('name', 'desc')" class="flex items-center justify-between px-3 py-1.5 hover:bg-gray-100 dark:hover:bg-gray-700 text-text-light dark:text-text-dark cursor-pointer"><span class="flex items-center gap-2"><span class="material-icons text-base">arrow_downward</span> Name</span></a></li>
<li><a @click="setSort('createdAt', 'asc')" class="flex items-center justify-between px-3 py-1.5 hover:bg-gray-100 dark:hover:bg-gray-700 text-text-light dark:text-text-dark cursor-pointer">Date Created</a></li>
<li><a @click="setSort('size', 'desc')" class="flex items-center justify-between px-3 py-1.5 hover:bg-gray-100 dark:hover:bg-gray-700 text-text-light dark:text-text-dark cursor-pointer">File Size</a></li>
<li class="bg-gray-200 dark:bg-gray-600"><a @click="setSort('updatedAt', 'desc')" class="flex items-center justify-between px-3 py-1.5 font-semibold text-primary dark:text-white cursor-pointer">Last Modified <span class="material-icons text-lg">check</span></a></li>
</ul>
</div>
</div>
<div class="flex items-center border border-border-light dark:border-border-dark rounded-md bg-card-light dark:bg-card-dark h-full">
<button :class="{ 'bg-gray-200 dark:bg-gray-600 rounded-l-md': view === 'grid' }" @click="view = 'grid'" class="p-2 text-text-light dark:text-text-dark h-full">
<span class="material-icons text-xl">grid_view</span>
</button>
<button :class="{ 'bg-gray-200 dark:bg-gray-600 rounded-r-md': view === 'list' }" @click="view = 'list'" class="p-2 text-text-light dark:text-text-dark h-full">
<span class="material-icons text-xl">list</span>
</button>
</div>
</div>
</div>
<div class="w-full" v-if="selectedItems.length > 0">
<div class="flex flex-col sm:flex-row justify-between items-center p-2 bg-blue-100 dark:bg-blue-900/30 rounded-lg">
<div class="flex items-center gap-2 mb-2 sm:mb-0 sm:mr-4 whitespace-nowrap">
<p class="font-medium text-primary dark:text-blue-300 text-sm">{{ selectedItems.length }} items selected</p>
</div>
<div class="flex gap-1.5 flex-wrap justify-center">
<button @click="showCopyModal = true" class="flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-white dark:bg-card-dark text-text-light dark:text-text-dark border border-border-light dark:border-border-dark shadow-sm text-xs">
<span class="material-icons text-base">content_copy</span>
<span>Copy</span>
</button>
<button @click="showMoveModal = true" class="flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-white dark:bg-card-dark text-text-light dark:text-text-dark border border-border-light dark:border-border-dark shadow-sm text-xs">
<span class="material-icons text-base">drive_file_move</span>
<span>Move</span>
</button>
<button @click="showDeleteModal = true" class="flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-red-600 text-white text-xs">
<span class="material-icons text-base">delete</span>
<span>Delete</span>
</button>
</div>
</div>
</div>
</div>
<div class="mb-6">
<h2 class="text-lg font-semibold text-text-light dark:text-text-dark mb-3">Folders</h2>
<div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4" v-if="view === 'grid'">
<div v-for="folder in folders" :key="folder.id" class="relative group bg-card-light dark:bg-card-dark rounded-lg shadow-around border-2 border-transparent p-2.5 flex items-center gap-3" :class="{'bg-blue-50 dark:bg-blue-900/20': selectedItems.includes(folder.id)}">
<input v-model="selectedItems" :value="folder.id" type="checkbox" class="form-checkbox h-4 w-4 rounded-full text-primary bg-white/50 border-gray-400/70 focus:ring-0 focus:ring-offset-0"/>
<span class="material-icons text-subtext-light dark:text-subtext-dark text-2xl">folder</span>
<div class="flex-grow">
<input v-if="renamingFolderId === folder.id" v-model="renamingName" @keyup.enter="renameItem(folder.id, renamingName)" @blur="renameItem(folder.id, renamingName)" class="font-semibold text-text-light dark:text-text-dark bg-gray-100 dark:bg-gray-800 rounded-md px-2 py-0.5 border border-primary focus:outline-none focus:ring-1 focus:ring-primary w-full text-sm" type="text" ref="renameInput"/>
<p v-else @dblclick="startRenaming(folder)" class="font-semibold text-text-light dark:text-text-dark truncate text-sm">{{ folder.name }}</p>
<p class="text-xs text-subtext-light dark:text-subtext-dark">{{ getFolderItemCount(folder.id) }} videos</p>
</div>
</div>
</div>
<div class="bg-card-light dark:bg-card-dark rounded-lg shadow-sm text-sm" v-if="view === 'list'">
<div class="space-y-px">
<div v-for="folder in folders" :key="folder.id" class="flex items-center p-2.5 hover:bg-gray-50 dark:hover:bg-gray-800" :class="{'bg-blue-50 dark:bg-blue-900/20': selectedItems.includes(folder.id)}">
<input v-model="selectedItems" :value="folder.id" type="checkbox" class="form-checkbox h-4 w-4 rounded-full text-primary bg-gray-100 border-gray-300 focus:ring-0 focus:ring-offset-0 dark:bg-gray-600 dark:border-gray-500"/>
<span class="material-icons text-subtext-light dark:text-subtext-dark text-xl mx-3">folder</span>
<div class="font-semibold text-text-light dark:text-text-dark flex-grow">
<input v-if="renamingFolderId === folder.id" v-model="renamingName" @keyup.enter="renameItem(folder.id, renamingName)" @blur="renameItem(folder.id, renamingName)" class="font-semibold text-text-light dark:text-text-dark bg-gray-100 dark:bg-gray-800 rounded-md px-2 py-0.5 border border-primary focus:outline-none focus:ring-1 focus:ring-primary w-full text-sm" type="text" ref="renameInput"/>
<span v-else @dblclick="startRenaming(folder)">{{ folder.name }}</span>
</div>
<span class="text-xs text-subtext-light dark:text-subtext-dark">{{ getFolderItemCount(folder.id) }} videos</span>
</div>
</div>
</div>
</div>
<div>
<h2 class="text-lg font-semibold text-text-light dark:text-text-dark mb-3">Videos</h2>
<div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4" v-if="view === 'grid'">
<div v-for="video in videos" :key="video.id" class="relative group bg-card-light dark:bg-card-dark rounded-lg shadow-around overflow-hidden border-2 border-transparent" :class="{'bg-blue-50 dark:bg-blue-900/20': selectedItems.includes(video.id), 'opacity-60': video.isMissing}">
<input v-model="selectedItems" :value="video.id" type="checkbox" class="form-checkbox h-4 w-4 rounded-full text-primary bg-white/50 border-gray-400/70 focus:ring-0 focus:ring-offset-0 absolute top-2.5 left-2.5 z-10"/>
<div class="aspect-video bg-gray-200 dark:bg-gray-700 flex items-center justify-center relative">
<span class="material-icons text-gray-400 dark:text-gray-500 text-4xl opacity-50 group-hover:opacity-20 transition-opacity" :class="{'text-blue-500 dark:text-blue-400': !video.isMissing}">
{{ video.isMissing ? 'error_outline' : 'movie' }}
</span>
<div v-if="!video.isMissing" class="absolute inset-0 bg-black/20 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer" @click="playVideo(video)">
<button class="bg-white/30 backdrop-blur-sm rounded-full p-2 text-white w-12 h-12 flex items-center justify-center">
<span class="material-icons text-3xl">play_arrow</span>
</button>
</div>
<div v-else class="absolute inset-0 bg-black/50 flex items-center justify-center text-white">
<span class="material-icons text-lg">error_outline</span>
</div>
<span v-if="!video.isMissing && video.dimensions" class="absolute bottom-1.5 right-1.5 bg-black/50 text-white text-xs px-1.5 py-0.5 rounded">
{{ video.dimensions }}
</span>
</div>
<div class="p-2.5">
<input v-if="renamingVideoId === video.id" v-model="renamingName" @keyup.enter="renameItem(video.id, renamingName)" @blur="renameItem(video.id, renamingName)" class="font-semibold text-text-light dark:text-text-dark bg-gray-100 dark:bg-gray-800 rounded-md px-2 py-0.5 border border-primary focus:outline-none focus:ring-1 focus:ring-primary w-full text-sm" type="text" ref="renameInput"/>
<p v-else @dblclick="startRenaming(video)" class="font-semibold text-text-light dark:text-text-dark truncate text-sm">{{ video.name }}</p>
<p class="text-xs text-subtext-light dark:text-subtext-dark mt-1">
{{ video.mimeType?.split('/')[1]?.toUpperCase() || 'FILE' }} · {{ formatFileSize(video.size) }}
</p>
<span v-if="video.isMissing" class="text-red-500 dark:text-red-400 text-xs font-semibold mt-1 inline-block">
Source Missing
</span>
</div>
</div>
</div>
<div class="bg-card-light dark:bg-card-dark rounded-lg shadow-sm overflow-hidden text-sm" v-if="view === 'list'">
<table class="w-full text-left text-subtext-light dark:text-subtext-dark">
<thead class="text-xs text-subtext-light dark:text-subtext-dark uppercase bg-gray-50 dark:bg-gray-700">
<tr>
<th class="p-3" scope="col">
<input v-model="selectAll" type="checkbox" class="form-checkbox h-4 w-4 rounded-full text-primary bg-gray-100 border-gray-300 focus:ring-0 focus:ring-offset-0 dark:bg-gray-600 dark:border-gray-500"/>
</th>
<th class="p-3" scope="col">File Name</th>
<th class="p-3" scope="col">Format</th>
<th class="p-3" scope="col">Dimensions</th>
<th class="p-3" scope="col">File Size</th>
<th class="p-3" scope="col"></th>
</tr>
</thead>
<tbody>
<tr v-for="video in videos" :key="video.id" class="border-t dark:border-border-dark hover:bg-gray-50 dark:hover:bg-gray-800" :class="{'bg-blue-50 dark:bg-blue-900/20': selectedItems.includes(video.id)}">
<td class="p-3">
<input v-model="selectedItems" :value="video.id" type="checkbox" class="form-checkbox h-4 w-4 rounded-full text-primary bg-gray-100 border-gray-300 focus:ring-0 focus:ring-offset-0 dark:bg-gray-600 dark:border-gray-500"/>
</td>
<td class="p-3 font-medium text-text-light dark:text-text-dark">
<div class="flex items-center gap-3">
<div class="relative group w-16 h-10 rounded-md overflow-hidden bg-gray-200 dark:bg-gray-700 flex-shrink-0">
<span class="material-icons text-gray-400 dark:text-gray-500 text-2xl absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 group-hover:opacity-20 transition-opacity" :class="{'text-blue-500 dark:text-blue-400': !video.isMissing}">
{{ video.isMissing ? 'error_outline' : 'movie' }}
</span>
<div v-if="!video.isMissing" class="absolute inset-0 bg-black/20 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer" @click="playVideo(video)">
<button class="bg-white/30 backdrop-blur-sm rounded-full w-8 h-8 flex items-center justify-center text-white">
<span class="material-icons text-xl">play_arrow</span>
</button>
</div>
</div>
<input v-if="renamingVideoId === video.id" v-model="renamingName" @keyup.enter="renameItem(video.id, renamingName)" @blur="renameItem(video.id, renamingName)" class="font-semibold text-text-light dark:text-text-dark bg-gray-100 dark:bg-gray-800 rounded-md px-2 py-0.5 border border-primary focus:outline-none focus:ring-1 focus:ring-primary w-full text-sm" type="text" ref="renameInput"/>
<span v-else @dblclick="startRenaming(video)">{{ video.name }}</span>
</div>
</td>
<td class="p-3">{{ video.mimeType?.split('/')[1]?.toUpperCase() || 'FILE' }}</td>
<td class="p-3">{{ video.dimensions || '-' }}</td>
<td class="p-3">{{ formatFileSize(video.size) }}</td>
<td class="p-3 text-right">
<span v-if="video.isMissing" class="text-red-500 dark:text-red-400 text-xs font-semibold bg-red-100 dark:bg-red-900/30 px-2 py-1 rounded-full">
Source Missing
</span>
</td>
</tr>
</tbody>
</table>
</div>
</div>
<!-- Delete Modal -->
<div v-if="showDeleteModal" @keydown.esc="showDeleteModal = false" class="fixed inset-0 z-50 overflow-y-auto" role="dialog">
<div class="flex items-end justify-center min-h-screen px-4 pt-4 pb-20 text-center sm:block sm:p-0">
<div @click="showDeleteModal = false" class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"></div>
<span class="hidden sm:inline-block sm:align-middle sm:h-screen">​</span>
<div class="inline-block align-bottom bg-card-light dark:bg-card-dark rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
<div class="bg-card-light dark:bg-card-dark px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
<div class="sm:flex sm:items-start">
<div class="mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-red-100 dark:bg-red-900/30 sm:mx-0 sm:h-10 sm:w-10">
<span class="material-icons text-red-600 dark:text-red-400">warning</span>
</div>
<div class="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left">
<h3 class="text-lg leading-6 font-medium text-text-light dark:text-text-dark">
Delete Items
</h3>
<div class="mt-2">
<p class="text-sm text-subtext-light dark:text-subtext-dark">
Are you sure you want to delete the {{ selectedItems.length }} selected items? This action cannot be undone.
</p>
</div>
</div>
</div>
</div>
<div class="bg-background-light dark:bg-background-dark px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
<button @click="deleteSelectedItems" class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-red-600 text-base font-medium text-white hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 sm:ml-3 sm:w-auto sm:text-sm">
Delete
</button>
<button @click="showDeleteModal = false" class="mt-3 w-full inline-flex justify-center rounded-md border border-border-light dark:border-border-dark shadow-sm px-4 py-2 bg-card-light dark:bg-card-dark text-base font-medium text-text-light dark:text-text-dark hover:bg-gray-50 dark:hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary sm:mt-0 sm:w-auto sm:text-sm">
Cancel
</button>
</div>
</div>
</div>
</div>
<!-- Move/Copy Modal -->
<div v-if="showMoveModal || showCopyModal" @keydown.esc="closeModals" class="fixed inset-0 z-50 overflow-y-auto" role="dialog">
<div class="flex items-end justify-center min-h-screen px-4 pt-4 pb-20 text-center sm:block sm:p-0">
<div @click="closeModals" class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"></div>
<span class="hidden sm:inline-block sm:align-middle sm:h-screen">​</span>
<div class="inline-block align-bottom bg-card-light dark:bg-card-dark rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
<div class="bg-card-light dark:bg-card-dark px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
<div class="sm:flex sm:items-start">
<div class="mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-blue-100 dark:bg-blue-900/30 sm:mx-0 sm:h-10 sm:w-10">
<span class="material-icons text-primary dark:text-blue-400">
{{ showMoveModal ? 'drive_file_move' : 'content_copy' }}
</span>
</div>
<div class="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left w-full">
<h3 class="text-lg leading-6 font-medium text-text-light dark:text-text-dark">
{{ showMoveModal ? 'Move Items' : 'Copy Items' }}
</h3>
<div class="mt-4">
<p class="text-sm text-subtext-light dark:text-subtext-dark mb-2">
Select a destination folder:
</p>
<div class="border border-border-light dark:border-border-dark rounded-lg p-3 space-y-2 h-48 overflow-y-auto">
<a @click="selectDestinationFolder(null)" :class="{'bg-blue-100 dark:bg-blue-900/40': selectedDestinationFolder === null}" class="flex items-center p-2 rounded-md hover:bg-gray-100 dark:hover:bg-gray-700 cursor-pointer">
<span class="material-icons text-subtext-light dark:text-subtext-dark mr-2">folder</span>
<span class="text-text-light dark:text-text-dark">Media Library</span>
</a>
<a @click="selectDestinationFolder(folder.id)" v-for="folder in allFolders" :key="folder.id" :class="{'bg-blue-100 dark:bg-blue-900/40': selectedDestinationFolder === folder.id, 'ml-6': folder.parentId}" class="flex items-center p-2 rounded-md hover:bg-gray-100 dark:hover:bg-gray-700 cursor-pointer">
<span class="material-icons text-subtext-light dark:text-subtext-dark mr-2">folder</span>
<span class="text-text-light dark:text-text-dark">{{ folder.name }}</span>
</a>
</div>
</div>
</div>
</div>
</div>
<div class="bg-background-light dark:bg-background-dark px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
<button @click="executeMoveCopy" :disabled="selectedDestinationFolder === null" class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-primary text-base font-medium text-white hover:bg-blue-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary sm:ml-3 sm:w-auto sm:text-sm" :class="{'opacity-50 cursor-not-allowed': selectedDestinationFolder === null}">
{{ showMoveModal ? 'Move' : 'Copy' }}
</button>
<button @click="closeModals" class="mt-3 w-full inline-flex justify-center rounded-md border border-border-light dark:border-border-dark shadow-sm px-4 py-2 bg-card-light dark:bg-card-dark text-base font-medium text-text-light dark:text-text-dark hover:bg-gray-50 dark:hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary sm:mt-0 sm:w-auto sm:text-sm">
Cancel
</button>
</div>
</div>
</div>
</div>
<!-- Toast notifications -->
<div v-if="toast.message" class="fixed top-5 right-5 z-50 py-3 px-6 rounded-lg shadow-lg flex items-center" :class="{'bg-green-500 text-white': toast.type === 'success', 'bg-red-500 text-white': toast.type === 'error'}">
<span class="material-icons mr-2">
{{ toast.type === 'success' ? 'check_circle' : 'error' }}
</span>
<span>{{ toast.message }}</span>
</div>
<!-- Video Player Modal -->
<div v-if="currentVideo" @click="closeVideoPlayer" class="fixed inset-0 bg-black/80 flex items-center justify-center z-50" id="video-popup">
<div class="bg-card-dark rounded-lg shadow-2xl w-full max-w-4xl relative" @click.stop>
<div class="aspect-video">
<video ref="videoPlayer" class="w-full h-full rounded-t-lg" controls>
<source :src="`${API_BASE_URL}/api/stream/video/${currentVideo.id}`" :type="currentVideo.mimeType || 'video/mp4'"/>
Your browser does not support the video tag.
</video>
</div>
<div class="p-6">
<h3 class="text-2xl font-bold text-text-dark">{{ currentVideo.name }}</h3>
<div class="flex items-center gap-4 text-lg text-subtext-dark mt-2">
<span>{{ currentVideo.mimeType?.split('/')[1]?.toUpperCase() || 'FILE' }}</span>
<span>·</span>
<span>{{ currentVideo.dimensions || 'N/A' }}</span>
<span>·</span>
<span>{{ formatFileSize(currentVideo.size) }}</span>
</div>
</div>
</div>
</div>
</div>
</template>

<script>
import { mapState, mapActions } from 'pinia';
import { useMediaStore } from '../stores/media';

export default {
  name: 'MediaLibrary',
  data() {
    return {
      view: 'grid', // 'grid' or 'list'
      sortOpen: false,
      showDeleteModal: false,
      showMoveModal: false,
      showCopyModal: false,
      selectedDestinationFolder: null,
      renamingFolderId: null,
      renamingVideoId: null,
      renamingName: '',
      searchQuery: '',
      selectAll: false,
      sortBy: 'updatedAt',
      sortOrder: 'desc',
      currentVideo: null,
      toast: {
        message: '',
        type: 'success' // 'success' or 'error'
      },
      toastTimeout: null
    };
  },
  computed: {
    ...mapState(useMediaStore, [
      'items', 
      'currentFolderId', 
      'currentPath', 
      'selectedItems',
      'allFolders'
    ]),
    folders() {
      return this.items
        .filter(item => item.type === 'folder')
        .sort(this.sortItems);
    },
    videos() {
      return this.items
        .filter(item => item.type === 'file')
        .sort(this.sortItems);
    },
    API_BASE_URL() {
      return process.env.VUE_APP_API_BASE_URL || 'http://localhost:3001';
    }
  },
  watch: {
    selectedItems: {
      handler(newVal) {
        this.selectAll = newVal.length > 0 && newVal.length === [...this.folders, ...this.videos].length;
      },
      deep: true
    },
    selectAll(newVal) {
      if (newVal) {
        this.selectedItems = [...this.folders, ...this.videos].map(item => item.id);
      } else {
        this.selectedItems = [];
      }
    }
  },
  methods: {
    ...mapActions(useMediaStore, {
      fetchFolderContents: 'fetchFolderContents',
      createFolder: 'createFolder',
      addFilesToLibrary: 'addFiles',
      renameItem: 'renameItem',
      moveItem: 'moveItem',
      deleteItem: 'deleteItem',
      searchItems: 'searchItems',
      setCurrentFolder: 'setCurrentFolder',
      fetchCurrentPath: 'fetchCurrentPath'
    }),
    async navigateToFolder(folderId) {
      this.setCurrentFolder(folderId);
      await this.fetchFolderContents(folderId);
      await this.fetchCurrentPath(folderId);
      this.selectedItems = []; // Clear selections when changing folders
    },
    navigateToBreadcrumb(index) {
      const folder = this.currentPath[index];
      this.navigateToFolder(folder.id);
    },
    async createNewFolder() {
      try {
        const folderName = prompt('Enter folder name:');
        if (folderName) {
          await this.createFolder({
            name: folderName,
            parentId: this.currentFolderId
          });
          this.showToast('Folder created successfully', 'success');
          await this.fetchFolderContents(this.currentFolderId);
        }
      } catch (error) {
        this.showToast('Failed to create folder', 'error');
        console.error(error);
      }
    },
    async addFiles() {
      try {
        const response = await fetch(`${this.API_BASE_URL}/api/media/select-files`);
        const data = await response.json();
        
        if (data.files && data.files.length > 0) {
          const files = data.files.map(path => ({ path }));

          await this.addFilesToLibrary({
            files,
            parentId: this.currentFolderId
          });

          this.showToast(`${files.length} files added successfully`, 'success');
          await this.fetchFolderContents(this.currentFolderId);
        }
      } catch (error) {
        this.showToast('Failed to add files', 'error');
        console.error(error);
      }
    },
    startRenaming(item) {
      this.renamingName = item.name;
      if (item.type === 'folder') {
        this.renamingFolderId = item.id;
      } else {
        this.renamingVideoId = item.id;
      }
      this.$nextTick(() => {
        if (this.$refs.renameInput && this.$refs.renameInput[0]) {
          this.$refs.renameInput[0].focus();
        }
      });
    },
    async deleteSelectedItems() {
      try {
        for (const itemId of this.selectedItems) {
          await this.deleteItem(itemId);
        }
        this.showToast(`${this.selectedItems.length} items deleted successfully`, 'success');
        this.showDeleteModal = false;
        this.selectedItems = [];
        await this.fetchFolderContents(this.currentFolderId);
      } catch (error) {
        this.showToast('Failed to delete items', 'error');
        console.error(error);
      }
    },
    selectDestinationFolder(folderId) {
      this.selectedDestinationFolder = folderId;
    },
    async executeMoveCopy() {
      try {
        if (this.selectedDestinationFolder === null) {
          this.showToast('Please select a destination folder', 'error');
          return;
        }

        for (const itemId of this.selectedItems) {
          if (this.showMoveModal) {
            await this.moveItem({ id: itemId, parentId: this.selectedDestinationFolder });
          }
        }

        const action = this.showMoveModal ? 'moved' : 'copied';
        this.showToast(`${this.selectedItems.length} items ${action} successfully`, 'success');
        this.closeModals();
        this.selectedItems = [];
        await this.fetchFolderContents(this.currentFolderId);
      } catch (error) {
        this.showToast(`Failed to ${this.showMoveModal ? 'move' : 'copy'} items`, 'error');
        console.error(error);
      }
    },
    closeModals() {
      this.showMoveModal = false;
      this.showCopyModal = false;
      this.selectedDestinationFolder = null;
    },
    playVideo(video) {
      this.currentVideo = video;
    },
    closeVideoPlayer() {
      this.currentVideo = null;
      if (this.$refs.videoPlayer) {
        this.$refs.videoPlayer.pause();
        this.$refs.videoPlayer.currentTime = 0;
      }
    },
    setSort(field, order) {
      this.sortBy = field;
      this.sortOrder = order;
      this.sortOpen = false;
    },
    sortItems(a, b) {
      let result = 0;
      switch (this.sortBy) {
        case 'name':
          result = a.name.localeCompare(b.name);
          break;
        case 'createdAt':
          result = new Date(a.createdAt) - new Date(b.createdAt);
          break;
        case 'updatedAt':
          result = new Date(a.updatedAt) - new Date(b.updatedAt);
          break;
        case 'size':
          result = (a.size || 0) - (b.size || 0);
          break;
        default:
          result = a.name.localeCompare(b.name);
      }
      
      return this.sortOrder === 'asc' ? result : -result;
    },
    getFolderItemCount(folderId) {
      return this.items.filter(item => item.parentId === folderId).length;
    },
    formatFileSize(bytes) {
      if (bytes === null || bytes === undefined) return 'N/A';
      if (bytes === 0) return '0 Bytes';
      
      const k = 1024;
      const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    },
    showToast(message, type) {
      this.toast = { message, type };
      
      if (this.toastTimeout) {
        clearTimeout(this.toastTimeout);
      }
      
      this.toastTimeout = setTimeout(() => {
        this.toast = { message: '', type: 'success' };
      }, 3000);
    }
  },
  async mounted() {
    await this.fetchFolderContents(null);
    await this.fetchCurrentPath(null);
  }
};
</script>

<style scoped>
.form-checkbox:focus,
.form-checkbox:checked:focus {
  outline: none !important;
  box-shadow: none !important;
  border-color: #007AFF !important;
}

.shadow-around {
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.1) !important;
}

.text-text-light {
  color: #1F2937;
}

.text-text-dark {
  color: #F9FAFB;
}

.text-subtext-light {
  color: #6B7280;
}

.text-subtext-dark {
  color: #9CA3AF;
}

.bg-background-light {
  background-color: #F9FAFB;
}

.bg-background-dark {
  background-color: #111827;
}

.bg-card-light {
  background-color: #FFFFFF;
}

.bg-card-dark {
  background-color: #1F2937;
}

.border-border-light {
  border-color: #E5E7EB;
}

.border-border-dark {
  border-color: #374151;
}

/* Handle dark mode classes */
.dark .text-text-light {
  color: #F9FAFB;
}

.dark .text-subtext-light {
  color: #9CA3AF;
}

.dark .bg-background-light {
  background-color: #111827;
}

.dark .bg-card-light {
  background-color: #1F2937;
}

.dark .border-border-light {
  border-color: #374151;
}

/* Selected item styling */
.bg-blue-50 {
  background-color: rgba(219, 234, 254, 1);
}

.dark .bg-blue-900\/20 {
  background-color: rgba(30, 58, 138, 0.2);
}

/* Ensure proper spacing */
.space-y-px > :not([hidden]) ~ :not([hidden]) {
  --tw-space-y-reverse: 0;
  margin-top: calc(1px * calc(1 - var(--tw-space-y-reverse)));
  margin-bottom: calc(1px * var(--tw-space-y-reverse));
}
</style>

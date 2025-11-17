import { MediaItem } from '../../models/MediaItem.js';
import fs from 'fs';
import path from 'path';
import { processMediaItem } from '../services/mediaProcessorService.js';

export const listMediaItems = async (req, res) => {
    const { parentId } = req.query;
    const userId = req.user.id; // Assuming user ID is available from authentication middleware

    try {
        // Use Sequelize to find media items
        const whereClause = { userId };
        if (parentId) {
            whereClause.parentId = parentId;
        } else {
            whereClause.parentId = null; // Top-level items only
        }

        const items = await MediaItem.findAll({
            where: whereClause,
            order: [
                ['type', 'ASC'], // Folders first
                ['name', 'ASC']
            ]
        });

        res.json({ success: true, data: items });
    } catch (error) {
        console.error('Error listing media items:', error);
        res.status(500).json({ success: false, message: 'Failed to list media items.' });
    }
};

export const createFolder = async (req, res) => {
    const { name, parentId } = req.body;
    const userId = req.user.id;

    if (!name) {
        return res.status(400).json({ success: false, message: 'Folder name is required.' });
    }

    try {
        const newFolder = await MediaItem.create({
            name,
            type: 'folder',
            parentId: parentId || null,
            userId,
            status: 'available' // Folders are immediately available
        });
        
        res.status(201).json({ 
            success: true, 
            message: 'Folder created successfully.', 
            id: newFolder.id 
        });
    } catch (error) {
        console.error('Error creating folder:', error);
        res.status(500).json({ success: false, message: 'Failed to create folder.' });
    }
};

export const addFiles = async (req, res) => {
    const { files, parentId } = req.body; // files is an array of { name, path, size, mimeType }
    const userId = req.user.id;

    if (!Array.isArray(files) || files.length === 0) {
        return res.status(400).json({ success: false, message: 'No files provided.' });
    }

    try {
        const insertedFiles = [];
        for (const file of files) {
            // Create the media item record
            console.log(`Adding file: ${file.name} at path: ${file.path}`);
            const newFile = await MediaItem.create({
                name: file.name,
                type: 'file',
                filePath: file.path, // Using filePath as per Sequelize schema
                parentId: parentId || null,
                userId,
                status: 'processing' // Start in processing state
            });
            
            insertedFiles.push({ id: newFile.id, name: file.name, path: file.path });
            
            // Trigger metadata extraction and thumbnail generation in the background
            // Don't wait for it to complete, return response immediately
            processMediaItem(newFile.id).catch(error => {
                console.error(`Error processing media item ${newFile.id}:`, error);
            });
        }
        
        res.status(201).json({ 
            success: true, 
            message: 'Files added successfully.', 
            data: insertedFiles 
        });
    } catch (error) {
        console.error('Error adding files:', error);
        res.status(500).json({ success: false, message: 'Failed to add files.' });
    }
};

export const renameMediaItem = async (req, res) => {
    const { id } = req.params;
    const { newName } = req.body;
    const userId = req.user.id;

    if (!newName) {
        return res.status(400).json({ success: false, message: 'New name is required.' });
    }

    try {
        const item = await MediaItem.findOne({
            where: { id, userId }
        });
        
        if (!item) {
            return res.status(404).json({ success: false, message: 'Media item not found or not authorized.' });
        }
        
        await item.update({ name: newName });
        
        res.json({ success: true, message: 'Media item renamed successfully.' });
    } catch (error) {
        console.error('Error renaming media item:', error);
        res.status(500).json({ success: false, message: 'Failed to rename media item.' });
    }
};

export const deleteMediaItems = async (req, res) => {
    const { ids } = req.body; // ids is an array of item IDs to delete
    const userId = req.user.id;

    if (!Array.isArray(ids) || ids.length === 0) {
        return res.status(400).json({ success: false, message: 'No media item IDs provided for deletion.' });
    }

    try {
        // Delete media items that belong to the user
        const result = await MediaItem.destroy({
            where: { 
                id: ids,
                userId: userId
            }
        });
        
        if (result === 0) {
            return res.status(404).json({ success: false, message: 'No media items found or authorized for deletion.' });
        }
        
        res.json({ success: true, message: `${result} media items deleted successfully.` });
    } catch (error) {
        console.error('Error deleting media items:', error);
        res.status(500).json({ success: false, message: 'Failed to delete media items.' });
    }
};

export const moveMediaItems = async (req, res) => {
    const { ids, newParentId } = req.body;
    const userId = req.user.id;

    if (!Array.isArray(ids) || ids.length === 0) {
        return res.status(400).json({ success: false, message: 'No media item IDs provided for moving.' });
    }

    try {
        // Optional: Validate newParentId exists and is a folder owned by the user
        if (newParentId) {
            const parent = await MediaItem.findOne({
                where: { 
                    id: newParentId, 
                    type: 'folder', 
                    userId: userId 
                }
            });
            
            if (!parent) {
                return res.status(404).json({ success: false, message: 'Destination folder not found or not authorized.' });
            }
        }

        // Update the parent_id for the specified items
        const result = await MediaItem.update(
            { parentId: newParentId || null },
            { 
                where: { 
                    id: ids, 
                    userId: userId 
                }
            }
        );

        if (result[0] === 0) { // result[0] contains the number of affected rows
            return res.status(404).json({ success: false, message: 'No media items found or authorized for moving.' });
        }
        
        res.json({ success: true, message: `${result[0]} media items moved successfully.` });
    } catch (error) {
        console.error('Error moving media items:', error);
        res.status(500).json({ success: false, message: 'Failed to move media items.' });
    }
};

export const copyMediaItems = async (req, res) => {
    const { ids, newParentId } = req.body;
    const userId = req.user.id;

    if (!Array.isArray(ids) || ids.length === 0) {
        return res.status(400).json({ success: false, message: 'No media item IDs provided for copying.' });
    }

    try {
        // Optional: Validate newParentId exists and is a folder owned by the user
        if (newParentId) {
            const parent = await MediaItem.findOne({
                where: { 
                    id: newParentId, 
                    type: 'folder', 
                    userId: userId 
                }
            });
            
            if (!parent) {
                return res.status(404).json({ success: false, message: 'Destination folder not found or not authorized.' });
            }
        }

        const copiedItems = [];
        
        for (const id of ids) {
            // Fetch original item
            const originalItem = await MediaItem.findByPk(id);
            
            if (!originalItem || originalItem.userId !== userId) {
                console.warn(`Media item ${id} not found or not authorized for copying.`);
                continue; // Skip to next item
            }

            // Create a new media item as a copy
            const newName = `Copy of ${originalItem.name}`;
            const newFile = await MediaItem.create({
                name: newName,
                type: originalItem.type,
                filePath: originalItem.filePath, // Copy the file path
                parentId: newParentId || null,
                userId,
                size: originalItem.size,
                duration: originalItem.duration,
                dimensions: originalItem.dimensions,
                mimeType: originalItem.mimeType,
                status: originalItem.status
            });

            copiedItems.push({ 
                id: newFile.id, 
                name: newName, 
                type: originalItem.type 
            });
        }

        if (copiedItems.length === 0) {
            return res.status(404).json({ success: false, message: 'No media items found or authorized for copying.' });
        }
        
        res.json({ 
            success: true, 
            message: `${copiedItems.length} media items copied successfully.`, 
            data: copiedItems 
        });
    } catch (error) {
        console.error('Error copying media items:', error);
        res.status(500).json({ success: false, message: 'Failed to copy media items.' });
    }
};

export const streamMediaItem = async (req, res) => {
    const { id } = req.params;
    const userId = req.user.id;

    try {
        const item = await MediaItem.findOne({
            where: { 
                id: id, 
                type: 'file', 
                userId: userId 
            }
        });

        if (!item || !item.filePath) {
            return res.status(404).json({ success: false, message: 'Media file not found or not authorized.' });
        }

        const filePath = item.filePath;

        // Check if file exists
        if (!fs.existsSync(filePath)) {
            // Update status to 'missing' in DB
            await item.update({ status: 'missing' });
            return res.status(404).json({ success: false, message: 'Source file not found on disk.' });
        }

        const stat = fs.statSync(filePath);
        const fileSize = stat.size;
        const range = req.headers.range;

        if (range) {
            const parts = range.replace(/bytes=/, '').split('-');
            const start = parseInt(parts[0], 10);
            const end = parts[1] ? parseInt(parts[1], 10) : fileSize - 1;
            const chunksize = (end - start) + 1;
            const file = fs.createReadStream(filePath, { start, end });
            const head = {
                'Content-Range': `bytes ${start}-${end}/${fileSize}`,
                'Accept-Ranges': 'bytes',
                'Content-Length': chunksize,
                'Content-Type': 'video/mp4', // Will be made dynamic
            };
            res.writeHead(206, head);
            file.pipe(res);
        } else {
            const mimeType = item.mimeType || 'application/octet-stream';
            const head = {
                'Content-Length': fileSize,
                'Content-Type': mimeType,
            };
            res.writeHead(200, head);
            fs.createReadStream(filePath).pipe(res);
        }
    } catch (error) {
        console.error('Error streaming media item:', error);
        res.status(500).json({ success: false, message: 'Failed to stream media item.' });
    }
};